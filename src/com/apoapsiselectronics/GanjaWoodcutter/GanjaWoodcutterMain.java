package com.apoapsiselectronics.GanjaWoodcutter;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.GroundItem;

@ScriptManifest(author = "GanjaSmuggler", category = Category.WOODCUTTING, name = "Ganja Woodcutter", description = "Chop any tree anywhere. Just start and fill the blank with tree names (e.g. Oak ,Willow ,Yew) then choose either drop or bank.", version = 1.0)
public class GanjaWoodcutterMain extends AbstractScript {
    public static final String VERSION = "2.3.6";

    private boolean isStarted = false;
    private boolean drop = true;
    private BotStates currentState = BotStates.START;
    private BotGUI botGUI = null;
    private List<String> woodList;
    private int ranMouseMovChance = 1;
    private int ranCamRotChance = 1;
    private int ranXpCheckChance = 1;
    private double treeDistance = 10.0;
    private Timer timer;
    private boolean canDraw = false;
    private Area destinationArea = null;
    private Tile previousTile = null;
    private boolean chopping = false;
    private GameObject tree = null;
    private boolean takeBirdNests = false;
    private boolean randMouseMovs = false;
    private boolean randXpCheck = false;
    private boolean randCameraRot = false;
    private boolean randWorldHop = false;
    private boolean randLogout = false;
    private Tile startTile = null;
    private Feedback feedback;
    private Antiban antiban;

    @Override
    public void onStart() {
        feedback = new Feedback();
        woodList = new ArrayList<>();
        timer = new Timer();
        antiban = new Antiban(this, timer);
        botGUI = new BotGUI(this, "Ganja Woodcutter");
        botGUI.Display();
    }

    @Override
    public int onLoop() {
        switch (currentState) {
            case START:
                log("START");
                if (isStarted) {
                    timer.reset();
                    getSkillTracker().start();
                    canDraw = true;
                    currentState = BotStates.CHECK_INV;
                    startTile = getLocalPlayer().getTile();
                } else
                    currentState = BotStates.START;
                break;
            case CHECK_INV:
                log("CHECK_INV");
                if (isStarted) {
                    if (getInventory().isFull())
                        currentState = BotStates.DROP_OR_BANK;
                    else
                        currentState = BotStates.CUT_TREE;
                }
                break;
            case CUT_TREE:
                log("CUT_TREE");
                if (isStarted) {
                    if (takeBirdNests && chopping) {
                        GroundItem birdNest = getGroundItems().closest("Bird nest");
                        if (birdNest != null) {
                            if (birdNest.interact("Take")) {
                                sleepUntil(() -> getInventory().contains("Bird nest"), 2000);
                                tree.interact("Chop down");
                            }
                        }
                    }

                    if (!chopping) {
                        tree = getGameObjects().closest(gameObject -> (treeDistance <= 0 || gameObject.distance(startTile) < treeDistance) && gameObject.getName().equalsIgnoreCase(woodList.get(Calculations.random(woodList.size()))));
                    }

                    if (getRandomManager().isSolving()) {
                        chopping = false;
                        tree = null;
                    }

                    if (tree != null && !chopping && !getLocalPlayer().isMoving() && tree.interact("Chop down")) {
                        if (randMouseMovs)
                            getMouse().move();
                        sleep(2000);
                        chopping = true;
                    }

                    if (tree != null && chopping && !getInventory().isFull() && tree.exists()) {
                        if (getLocalPlayer().isStandingStill()) {
                            tree.interact("Chop down");
                            getMouse().move();
                        }

                        if (randCameraRot)
                            antiban.RandomCameraRotation(ranCamRotChance);
                        if (randXpCheck)
                            antiban.RandomXPCheck(ranXpCheckChance, 320, 22);
                        if (randMouseMovs)
                            antiban.RandomMouseMovement(ranMouseMovChance);
                        if (randLogout)
                            antiban.RandomLogout();
                        if (randWorldHop)
                            antiban.RandomWorldHop();
                        antiban.UseSpecialAttack();

                        sleep(50);
                    } else {
                        if (randLogout && !getClient().isLoggedIn()) {
                            currentState = BotStates.LOGGED_OUT;
                        } else {
                            currentState = BotStates.CHECK_INV;
                        }
                        chopping = false;
                    }
                }
                break;
            case DROP_OR_BANK:
                log("DROP_OR_BANK");
                if (isStarted) {
                    if (drop)
                        currentState = BotStates.DROP;
                    else
                        currentState = BotStates.BANK;
                }
                break;
            case DROP:
                log("DROP");
                if (isStarted) {
                    Utilities.OpenTab(this, Tab.INVENTORY);
                    getInventory().dropAllExcept(item -> !item.getName().toLowerCase().contains("log"));
                    currentState = BotStates.CUT_TREE;
                }
                break;
            case BANK:
                log("BANK");
                if (isStarted) {
                    destinationArea = getBank().getClosestBankLocation().getArea(1);
                    if (!destinationArea.contains(getLocalPlayer())) {
                        previousTile = getLocalPlayer().getTile();
                        currentState = BotStates.WALK_TO_BANK;
                    } else {
                        Utilities.OpenTab(this, Tab.INVENTORY);
                        Utilities.BankAllItems(this);
                        currentState = BotStates.WALK_FROM_BANK;
                    }
                }
                break;
            case WALK_TO_BANK:
                log("WALK_TO_BANK");
                if (isStarted) {
                    if (!destinationArea.contains(getLocalPlayer())) {
                        Utilities.GoToArea(this, getBank().getClosestBankLocation().getArea(1));
                    } else {
                        currentState = BotStates.BANK;
                    }
                }
                break;
            case WALK_FROM_BANK:
                log("WALK_FROM_BANK");
                if (isStarted) {
                    if (!(previousTile.distance(getLocalPlayer().getTile()) < 2)) {
                        Utilities.GoToTile(this, previousTile);
                    } else {
                        currentState = BotStates.CUT_TREE;
                    }
                }
                break;
            case LOGGED_OUT:
                antiban.RandomLogout();

                sleep(1000);

                if (getClient().isLoggedIn())
                    currentState = BotStates.CUT_TREE;
                break;
        }

        if (getDialogues().canContinue()) {
            log("In dialogue");
            getDialogues().spaceToContinue();
        }

        return Calculations.random(50, 100);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPaint(Graphics2D graphics) {
        if (canDraw) {
            Graphics2D g = (Graphics2D) graphics.create();
            botGUI.DrawInGameGUI(g);
            g.dispose();
        }
    }

    @Override
    public void onExit() {
        botGUI.Close();
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean isStarted) {
        this.isStarted = isStarted;
    }

    public void setDrop(boolean drop) {
        this.drop = drop;
    }

    public List<String> getWoodList() {
        return woodList;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTakeBirdNests(boolean takeBirdNests) {
        this.takeBirdNests = takeBirdNests;
    }

    public void setUseSpecialAttack(boolean useSpecialAttack) {
        antiban.setUseSpecialAttack(useSpecialAttack);
    }

    public void setRandMouseMovs(boolean randMouseMovs) {
        this.randMouseMovs = randMouseMovs;
    }

    public void setRandXpCheck(boolean randXpCheck) {
        this.randXpCheck = randXpCheck;
    }

    public void setRandCameraRot(boolean randCameraRot) {
        this.randCameraRot = randCameraRot;
    }

    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setRanMouseMovChance(int ranMouseMovChance) {
        this.ranMouseMovChance = ranMouseMovChance;
    }

    public void setRanCamRotChance(int ranCamRotChance) {
        this.ranCamRotChance = ranCamRotChance;
    }

    public void setRanXpCheckChance(int ranXpCheckChance) {
        this.ranXpCheckChance = ranXpCheckChance;
    }

    public void setRandWorldHop(boolean randWorldHop) {
        this.randWorldHop = randWorldHop;
    }

    public Antiban getAntiban() {
        return antiban;
    }

    public void setRandLogout(boolean randLogout) {
        this.randLogout = randLogout;
    }

    public void setTreeDistance(double treeDistance) {
        this.treeDistance = treeDistance;
    }
}
