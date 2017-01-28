package com.apoapsiselectronics.GanjaWoodcutter;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.utilities.Timer;

public class Antiban {
    private AbstractScript context;
    private boolean useSpecialAttack;
    private int worldHopTime;
    private int breakTime = 0;
    private int workTime = 0;
    private boolean loggedOut = false;
    private boolean isMember = false;
    private Timer timer;
    private long hopElapsed;
    private long workElapsed;

    public Antiban(AbstractScript context, Timer timer) {
        this.context = context;
        this.timer = timer;
        worldHopTime = Calculations.random(25, 35);
        AbstractScript.log(String.valueOf(worldHopTime));
    }

    /**
     * Moves the camera randomly with a chance defined by denominator.
     *
     * @param denominator Denominator of probability function.
     */
    public void RandomCameraRotation(int denominator) {
        if (Calculations.random(denominator) == 1)
            context.getCamera().rotateTo(Calculations.random(100, 700), Calculations.random(100, 700));
    }

    /**
     * Moves the cursor on to the given child of the parent with a chance
     * specified by denominator. The method also has a 50% chance to look at the
     * combat status if UseSpecialAttack is set.
     *
     * @param denominator Denominator of probability function.
     * @param parent Parent widget.
     * @param child Child widget.
     */
    public void RandomXPCheck(int denominator, int parent, int child) {
        if (Calculations.random(denominator) == 1) {
            if (Calculations.random(2) == 1) {
                if (useSpecialAttack)
                    context.getTabs().openWithMouse(Tab.COMBAT);
                AbstractScript.sleep(Calculations.random(1000, 2000));
                context.getTabs().openWithMouse(Tab.INVENTORY);
            } else if (context.getTabs().openWithMouse(Tab.STATS)) {
                context.getMouse().move(context.getWidgets().getWidgetChild(parent, child).getRectangle());
                AbstractScript.sleep(Calculations.random(3000, 5000));
                context.getTabs().openWithMouse(Tab.INVENTORY);
                if (Calculations.random(2) == 1)
                    context.getMouse().moveMouseOutsideScreen();
                else
                    context.getMouse().move();
            }
        }
    }

    /**
     * Moves the cursor randomly with a chance defined by denominator.
     *
     * @param denominator Denominator of probability function.
     */
    public void RandomMouseMovement(int denominator) {
        if (Calculations.random(denominator) == 1)
            context.getMouse().move();
    }

    /**
     * Whenever possible, use special attack.
     */
    public void UseSpecialAttack() {
        if (useSpecialAttack && context.getEquipment().contains("Dragon axe") && context.getCombat().getSpecialPercentage() == 100) {
            context.getCombat().toggleSpecialAttack(true);
            context.getTabs().openWithMouse(Tab.INVENTORY);
            context.getMouse().move();
        }
    }

    /**
     * When the time comes, the method hops the world to another one.
     */
    public void RandomWorldHop() {
        if (timer.elapsed() - hopElapsed > worldHopTime * 60 * 1000) {
            World chosenWorld = context.getWorlds().getRandomWorld(world -> {
                int playerTotalLevel = 0;
                int[] levels = context.getClient().getLevels();

                for (int level : levels) {
                    playerTotalLevel += level;
                }
                return (isMember ? world.isMembers() : world.isF2P()) && (world.getMinimumLevel() < playerTotalLevel) && !world.isPVP() && !world.isDeadmanMode() && !world.isHighRisk() && !world.isLastManStanding();
            });

            context.getWorldHopper().hopWorld(chosenWorld);
            hopElapsed = timer.elapsed();
        }
    }

    /**
     * Logout for a specified time and login when time is expired.
     */
    public void RandomLogout() {
        if (loggedOut) {
            if (timer.elapsed() - workElapsed > (breakTime == 0 ? 5 : breakTime) * 60 * 1000) {
                AbstractScript.log("Trying to login.");
                context.getRandomManager().enableSolver(RandomEvent.LOGIN);
                loggedOut = false;
                workElapsed = timer.elapsed();
            }
        } else if (timer.elapsed() - workElapsed > (workTime == 0 ? 5 : workTime) * 60 * 1000) {
                AbstractScript.log("Trying to logout.");
                context.getRandomManager().disableSolver(RandomEvent.LOGIN);
                context.getTabs().logout();
                loggedOut = true;
            workElapsed = timer.elapsed();
        }
    }

    public void setUseSpecialAttack(boolean useSpecialAttack) {
        this.useSpecialAttack = useSpecialAttack;
    }

    public void setMember(boolean isMember) {
        this.isMember = isMember;
    }

    public void setWorldHopTime(int minutes) {
        this.worldHopTime = minutes;
    }

    public void setBreakTime(int minutes) {
        this.breakTime = minutes;
    }

    public void setWorkTime(int minutes) {
        this.workTime = minutes;
    }
}
