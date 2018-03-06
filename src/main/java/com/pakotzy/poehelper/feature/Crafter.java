package com.pakotzy.poehelper.feature;

import com.pakotzy.poehelper.Runner;
import com.pakotzy.poehelper.Utils;
import com.pakotzy.poehelper.event.CrafterEvent;
import com.sun.glass.ui.Robot;
import javafx.scene.control.TitledPane;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crafter extends Feature {
	/*
	 * Number of links
	 * (?:\s|^)[RGB](?:[-][RGB]){1}(?=\s|$)
	 *
	 * Number of sockets
	 * ^[RGB](?:[ -][RGB]){5}[ $]
	 *
	 * Needed colors
	 *^(?=(?:.*?R){1})(?=(?:.*?G){1})(?=(?:.*?B){2}).{1,11}$
	 * */

	@Override
	public TitledPane draw() {
		bind(getFeatureId(), 0);
		return new TitledPane();
	}

	@Override
	public void run(Integer id) {
		CrafterEvent event = (CrafterEvent) getEvent(id);

		ArrayList<Pattern> patterns = new ArrayList<>();
		// Find line
		patterns.add(Pattern.compile("(?:Sockets: )((?:[RGB][ -]){0,5})", Pattern.MULTILINE));
		// Find sockets
		if (event.getSockets() > 0)
			patterns.add(Pattern.compile(String.format("(^[RGB](?:[ -][RGB]){%d,}[ $])", event.getSockets() - 1)));
		// Find links
		if (event.getLinks() > 0) {
			patterns.add(Pattern.compile(String.format("((?:\\s|^)[RGB](?:[-][RGB]){%d,}(?=\\s|$))",
					event.getLinks() - 1)));
		}
		// Find colors
		if (event.getColorsSum() > 0)
			patterns.add(Pattern.compile(String.format("(^(?=(?:.*?R){%d})(?=(?:.*?G){%d})(?=(?:.*?B){%d}).{1,11}$)",
					event.getR(), event.getG(), event.getB())));

		Utils.executeOnEventThread(() -> Utils.writeToClipboard("Starting writer"));
		Utils.executeOnEventThread(() -> Runner.ROBOT.get().keyPress(KeyEvent.VK_SHIFT));

		while (true) {
			String oldItem = Utils.executeOnEventThread(Utils::readFromClipboard);

			String item = oldItem;
			while (item.equals(oldItem)) {
				item = Utils.executeOnEventThread(() -> {
					Runner.ROBOT.get().keyPress(KeyEvent.VK_CONTROL);
					Runner.ROBOT.get().keyPress(KeyEvent.VK_C);
					Runner.ROBOT.get().keyRelease(KeyEvent.VK_C);
					Runner.ROBOT.get().keyRelease(KeyEvent.VK_CONTROL);

					return Utils.readFromClipboard();
				});

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
			}

			if (match(item, patterns))
				break;

			Utils.executeOnEventThread(() -> {
				Runner.ROBOT.get().mousePress(Robot.MOUSE_LEFT_BTN);
				Runner.ROBOT.get().mouseRelease(Robot.MOUSE_LEFT_BTN);
			});
		}

		Utils.executeOnEventThread(() -> Runner.ROBOT.get().keyRelease(KeyEvent.VK_SHIFT));
	}

	private boolean match(String item, ArrayList<Pattern> patterns) {
		Matcher m;

		for (Pattern p : patterns) {
			m = p.matcher(item);
			if (m.find()) {
				item = m.group(1);
			} else {
				System.out.println("Not found " + p.pattern());
				System.out.println(item);
				return false;
			}
		}
		return true;

		/*
			Rarity: Rare
			Foe Beak
			Siege Axe
			--------
					One Handed Axe
			Quality: +19% (augmented)
					Physical Damage: 57-108 (augmented)
					Elemental Damage: 36-72 (augmented), 11-165 (augmented)
					Critical Strike Chance: 5.00%
					Attacks per Second: 1.50
			Weapon Range: 9
					--------
			Requirements:
			Level: 59 (unmet)
					Str: 119 (unmet)
					Dex: 82 (unmet)
					--------
			Sockets: R R-R G-R-R
					--------
			Item Level: 74
					--------
			Adds 10 to 21 Physical Damage
			+18 to Strength
			+25 to Dexterity
			Adds 36 to 72 Cold Damage
			Adds 11 to 165 Lightning Damage
			13% reduced Enemy Stun Threshold
					--------
			Note: ~price 1 chaos
		*/
	}

	@Override
	public void stop(Integer id) {
		Utils.executeOnEventThread(() -> Runner.ROBOT.get().keyRelease(KeyEvent.VK_SHIFT));
	}
}
