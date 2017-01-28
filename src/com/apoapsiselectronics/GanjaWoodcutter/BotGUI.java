package com.apoapsiselectronics.GanjaWoodcutter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;

public class BotGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    public final int WIDTH = 300;
    public final int HEIGHT = 340;
    private GanjaWoodcutterMain context;

    private Image backgroundImage = null;
    private JTextField tfTreeNames;
    private JTextField tfHopWorld;
    private JTextField tfBreakTime;
    private JTextField tfWorkTime;
    private JCheckBox chbBirdNests;
    private JCheckBox chbSpecialAttack;
    private JCheckBox chbRanMouseMov;
    private JCheckBox chbRanXpCheck;
    private JCheckBox chbRanCamRot;
    private JCheckBox chbHopWorld;
    private JCheckBox chbLogoutTimer;
    private JTextField tfField;
    private JSlider sRanMouseMov;
    private JSlider sRanXpCheck;
    private JSlider sRanCamRot;
    private JTextArea taFeedback;
    private JComboBox<String> cbDeposit;
    private Image ganjaIcon;
    private JButton btnStart;
    private JButton btnStop;

    public BotGUI(GanjaWoodcutterMain context, String title) {
        super(title);
        this.context = context;
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        InitializeComponents();

        try {
            backgroundImage = Utilities.LoadImage("http://i63.tinypic.com/2j48v94.png", 190, 111);
            ganjaIcon = Utilities.LoadImage("http://ai-i1.infcdn.net/icons_siandroid/png/200/1138/1138962.png", 20, 20);

        } catch (Exception e) {
            AbstractScript.log(e.getMessage());
        }
    }

    class ChbAntibanChanged implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JCheckBox source = (JCheckBox) e.getSource();

            if (source.isSelected()) {
                chbRanMouseMov.setEnabled(true);
                sRanMouseMov.setEnabled(true);
                chbRanCamRot.setEnabled(true);
                sRanCamRot.setEnabled(true);
                chbRanXpCheck.setEnabled(true);
                sRanXpCheck.setEnabled(true);

                context.setRanCamRotChance(sRanCamRot.getValue());
                context.setRanMouseMovChance(sRanMouseMov.getValue());
                context.setRanXpCheckChance(sRanXpCheck.getValue());
            } else {
                chbRanMouseMov.setSelected(false);
                chbRanMouseMov.setEnabled(false);
                sRanMouseMov.setEnabled(false);
                chbRanCamRot.setSelected(false);
                chbRanCamRot.setEnabled(false);
                sRanCamRot.setEnabled(false);
                chbRanXpCheck.setSelected(false);
                chbRanXpCheck.setEnabled(false);
                sRanXpCheck.setEnabled(false);
            }
        }
    }

    class ChbMemberChanged implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JCheckBox source = (JCheckBox) e.getSource();

            if (source.isSelected()) {
                chbBirdNests.setEnabled(true);
                chbSpecialAttack.setEnabled(true);

                context.getAntiban().setMember(true);
            } else {
                chbBirdNests.setEnabled(false);
                chbSpecialAttack.setEnabled(false);
                context.getAntiban().setMember(false);
            }
        }
    }

    class BtnStartClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!context.isStarted()) {
                if (!tfTreeNames.getText().equals("e.g. Magic tree, Oak, Yew, Tree")) {
                    String[] trees = tfTreeNames.getText().split(",");

                    for (String tree : trees) {
                        context.getWoodList().add(tree.trim());
                    }

                    if (((String) cbDeposit.getSelectedItem()).equalsIgnoreCase("drop logs")) {
                        context.setDrop(true);
                    } else if (((String) cbDeposit.getSelectedItem()).equalsIgnoreCase("bank logs")) {
                        context.setDrop(false);
                    }

                    btnStart.setEnabled(false);
                    btnStop.setEnabled(true);

                    context.setCanDraw(true);
                    context.setStarted(true);
                    if (tfField.getText().equals("")) {
                        context.setTreeDistance(-1.0);
                    } else {
                        context.setTreeDistance(Double.parseDouble(tfField.getText()));
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please specify the tree names.", "Ganja Woodcutter - Settings", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    class BtnStopClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (context.isStarted()) {
                context.setStarted(false);
                context.getWoodList().clear();
                context.setCanDraw(false);
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
            }
        }
    }

    class BtnDefaultClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sRanMouseMov.setValue(50);
            sRanXpCheck.setValue(150);
            sRanCamRot.setValue(35);
        }
    }

    class SRanMouseMovChanged implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            context.setRanMouseMovChance(source.getMaximum() - source.getValue());
        }
    }

    class SRanXpCheckChanged implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            context.setRanXpCheckChance(source.getMaximum() - source.getValue());
        }
    }

    class SRanCamRotChanged implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            context.setRanCamRotChance(source.getMaximum() - source.getValue());
        }
    }

    class BtnSubmitClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = taFeedback.getText();
            taFeedback.setText("");
            context.getFeedback().SendString(message, context.getClient().getUsername());
        }
    }

    class TfTreeNamesFocused implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            if (tfTreeNames.getText().equals("e.g. Magic tree, Oak, Yew, Tree")) {
                tfTreeNames.setText("");
                tfTreeNames.setForeground(Color.BLACK);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (tfTreeNames.getText().equals("")) {
                tfTreeNames.setForeground(new Color(0x84, 0x84, 0x84));
                tfTreeNames.setText("e.g. Magic tree, Oak, Yew, Tree");
            }
        }
    }

    private void InitializeComponents() {
        JPanel genericPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        genericPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT - 40));

        btnStart = new JButton();
        btnStart.setIcon(new ImageIcon(Utilities.LoadImage("http://cdn3.iconfinder.com/data/icons/buttons/512/Icon_3-128.png", 35, 35)));
        btnStart.setPreferredSize(new Dimension(40, 40));
        btnStart.addActionListener(new BtnStartClicked());

        btnStop = new JButton();
        btnStop.setIcon(new ImageIcon(Utilities.LoadImage("http://cdn3.iconfinder.com/data/icons/buttons/512/Icon_5-128.png", 35, 35)));
        btnStop.setPreferredSize(new Dimension(40, 40));
        btnStop.setEnabled(false);
        btnStop.addActionListener(new BtnStopClicked());

        genericPanel.add(btnStart);
        genericPanel.add(btnStop);

        JTabbedPane tabPanel = new JTabbedPane();
        tabPanel.setPreferredSize(new Dimension(WIDTH - 15, HEIGHT - 90));
        tabPanel.setBackground(new Color(0xBF, 0xBF, 0xBF));

        // *************** SETTINGS TAB ***************//
        tabPanel.addTab("Settings", InitializeSettingsComponents());

        // *************** ANTIBAN TAB ***************//
        tabPanel.addTab("Antiban", InitializeAntibanComponents());

        // **************** TIMER TAB ****************//
        tabPanel.addTab("Timer", InitializeTimerComponents());

        // *************** FEEDBACK TAB ***************//
        tabPanel.addTab("Feedback", InitializeFeedbackComponents());

        genericPanel.add(tabPanel);

        add(genericPanel);
    }

    private JPanel InitializeSettingsComponents() {
        JPanel pSettings = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel lblTreeNames = new JLabel("Enter your tree name(s):");
        pSettings.add(lblTreeNames);

        tfTreeNames = new JTextField(18);
        tfTreeNames.setForeground(new Color(0x84, 0x84, 0x84));
        tfTreeNames.setText("e.g. Magic tree, Oak, Yew, Tree");
        tfTreeNames.addFocusListener(new TfTreeNamesFocused());
        pSettings.add(tfTreeNames);

        JLabel lblSpace5 = new JLabel();
        lblSpace5.setPreferredSize(new Dimension(WIDTH - tfTreeNames.getWidth(), lblSpace5.getHeight()));
        pSettings.add(lblSpace5);

        cbDeposit = new JComboBox<>(new String[]
                {"Drop Logs", "Bank Logs"});
        pSettings.add(cbDeposit);

        JLabel lblSpace2 = new JLabel();
        lblSpace2.setPreferredSize(new Dimension(WIDTH - cbDeposit.getWidth(), lblSpace2.getHeight()));
        pSettings.add(lblSpace2);

        JLabel lblField = new JLabel("Working radius: ");
        pSettings.add(lblField);

        tfField = new JTextField(6);
        pSettings.add(tfField);

        JLabel lblTiles = new JLabel("tiles");
        pSettings.add(lblTiles);

        JLabel lblInfo = new JLabel(new ImageIcon(Utilities.LoadImage("http://cdn3.iconfinder.com/data/icons/buttons/512/Icon_17-128.png", 20, 20)));
        lblInfo.setToolTipText("For information checkout the Ganja Woodcutter forum post. It includes a great explanation about bot.");
        pSettings.add(lblInfo);

        JCheckBox chbMember = new JCheckBox("Member Account?");
        chbMember.addChangeListener(new ChbMemberChanged());
        pSettings.add(chbMember);

        JLabel lblSpace1 = new JLabel();
        lblSpace1.setPreferredSize(new Dimension(WIDTH - chbMember.getWidth(), lblSpace1.getHeight()));
        pSettings.add(lblSpace1);

        chbBirdNests = new JCheckBox("Take bird nests");
        chbBirdNests.addChangeListener((e) -> {
            if (chbBirdNests.isSelected())
                context.setTakeBirdNests(true);
            else
                context.setTakeBirdNests(false);
        });
        chbBirdNests.setEnabled(false);
        pSettings.add(chbBirdNests);

        JLabel lblSpace3 = new JLabel();
        lblSpace3.setPreferredSize(new Dimension(WIDTH - chbBirdNests.getWidth(), lblSpace3.getHeight()));
        pSettings.add(lblSpace3);

        chbSpecialAttack = new JCheckBox("Use special attack");
        chbSpecialAttack.addChangeListener((e) -> context.setUseSpecialAttack(chbSpecialAttack.isSelected()));
        chbSpecialAttack.setEnabled(false);
        pSettings.add(chbSpecialAttack);

        return pSettings;
    }

    private JPanel InitializeAntibanComponents() {
        JPanel pAntiban = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JCheckBox chbAntiban = new JCheckBox("Use Antiban?");
        chbAntiban.addChangeListener(new ChbAntibanChanged());
        pAntiban.add(chbAntiban);

        JButton btnDefault = new JButton("Default");
        btnDefault.addActionListener(new BtnDefaultClicked());
        pAntiban.add(btnDefault);

        chbRanMouseMov = new JCheckBox("Random mouse movements");
        chbRanMouseMov.addChangeListener((e) -> context.setRandMouseMovs(chbRanMouseMov.isSelected()));
        chbRanMouseMov.setEnabled(false);
        pAntiban.add(chbRanMouseMov);

        sRanMouseMov = new JSlider(2, 100, 50);
        sRanMouseMov.setMajorTickSpacing(10);
        sRanMouseMov.setPaintTicks(true);
        sRanMouseMov.addChangeListener(new SRanMouseMovChanged());
        pAntiban.add(sRanMouseMov);
        sRanMouseMov.setEnabled(false);

        chbRanXpCheck = new JCheckBox("Random XP check");
        chbRanXpCheck.addChangeListener((e) -> context.setRandXpCheck(chbRanXpCheck.isSelected()));
        chbRanXpCheck.setEnabled(false);
        pAntiban.add(chbRanXpCheck);

        sRanXpCheck = new JSlider(2, 500, 150);
        sRanXpCheck.setMajorTickSpacing(50);
        sRanXpCheck.setPaintTicks(true);
        sRanXpCheck.addChangeListener(new SRanXpCheckChanged());
        pAntiban.add(sRanXpCheck);
        sRanXpCheck.setEnabled(false);

        chbRanCamRot = new JCheckBox("Random camera rotation");
        chbRanCamRot.addChangeListener((e) -> context.setRandCameraRot(chbRanCamRot.isSelected()));
        chbRanCamRot.setEnabled(false);
        pAntiban.add(chbRanCamRot);

        sRanCamRot = new JSlider(2, 100, 35);
        sRanCamRot.setMajorTickSpacing(10);
        sRanCamRot.setPaintTicks(true);
        sRanCamRot.addChangeListener(new SRanCamRotChanged());
        pAntiban.add(sRanCamRot);
        sRanCamRot.setEnabled(false);

        //JPanel btnPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        //btnPanel2.setPreferredSize(new Dimension(WIDTH - 25, HEIGHT));
        //JButton btnDefault = new JButton("Default");
        //btnDefault.addActionListener(new BtnDefaultClicked());
        //btnPanel2.add(btnDefault);
        //pAntiban.add(btnPanel2);

        return pAntiban;
    }

    public JPanel InitializeTimerComponents() {
        JPanel pTimer = new JPanel(new FlowLayout(FlowLayout.LEFT));

        chbHopWorld = new JCheckBox("World Hop every");
        chbHopWorld.addChangeListener(e -> {
            context.setRandWorldHop(chbHopWorld.isSelected());
            tfHopWorld.setEnabled(chbHopWorld.isSelected());
        });
        pTimer.add(chbHopWorld);

        tfHopWorld = new JTextField(5);
        tfHopWorld.setEnabled(false);
        tfHopWorld.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                context.getAntiban().setWorldHopTime(Integer.parseInt(tfHopWorld.getText()));
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                context.getAntiban().setWorldHopTime(Integer.parseInt(tfHopWorld.getText()));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                context.getAntiban().setWorldHopTime(Integer.parseInt(tfHopWorld.getText()));
            }
        });
        pTimer.add(tfHopWorld);

        JLabel lblHopWorld = new JLabel(" minutes");
        pTimer.add(lblHopWorld);

        chbLogoutTimer = new JCheckBox("Break Timer");
        chbLogoutTimer.addChangeListener(e -> {
            context.setRandLogout(chbLogoutTimer.isSelected());
            tfWorkTime.setEnabled(chbLogoutTimer.isSelected());
            tfBreakTime.setEnabled(chbLogoutTimer.isSelected());
        });
        pTimer.add(chbLogoutTimer);

        JPanel pSpace = new JPanel();
        pSpace.setPreferredSize(new Dimension(WIDTH, lblHopWorld.getHeight()));
        pTimer.add(pSpace);

        JLabel lblWorkTime = new JLabel("Work Time: ");
        pTimer.add(lblWorkTime);

        tfWorkTime = new JTextField(8);
        tfWorkTime.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                context.getAntiban().setWorkTime(Integer.parseInt(tfWorkTime.getText()));
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                context.getAntiban().setWorkTime(Integer.parseInt(tfWorkTime.getText()));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                context.getAntiban().setWorkTime(Integer.parseInt(tfWorkTime.getText()));
            }
        });
        tfWorkTime.setEnabled(false);
        pTimer.add(tfWorkTime);

        JLabel lblWorkTimeMinutes = new JLabel(" minutes");
        pTimer.add(lblWorkTimeMinutes);

        JLabel lblBreakTime = new JLabel("Break Time: ");
        pTimer.add(lblBreakTime);

        tfBreakTime = new JTextField(8);
        tfBreakTime.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                context.getAntiban().setBreakTime(Integer.parseInt(tfBreakTime.getText()));
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                context.getAntiban().setBreakTime(Integer.parseInt(tfBreakTime.getText()));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                context.getAntiban().setBreakTime(Integer.parseInt(tfBreakTime.getText()));
            }
        });
        tfBreakTime.setEnabled(false);
        pTimer.add(tfBreakTime);

        JLabel lblBreakTimeMinutes = new JLabel(" minutes");
        pTimer.add(lblBreakTimeMinutes);

        return pTimer;
    }

    private JPanel InitializeFeedbackComponents() {
        JPanel pFeedback = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JLabel lblFeedback = new JLabel("<html>Please inform us about any suggestions<br>or bug issues.</html>");
        pFeedback.add(lblFeedback);

        taFeedback = new JTextArea(9, 20);
        taFeedback.setLineWrap(true);
        taFeedback.setAutoscrolls(true);
        taFeedback.setWrapStyleWord(true);

        JScrollPane spFeedback = new JScrollPane(taFeedback);
        pFeedback.add(spFeedback);

        JButton btnSubmit = new JButton("Submit");
        btnSubmit.addActionListener(new BtnSubmitClicked());
        pFeedback.add(btnSubmit);

        return pFeedback;
    }

    public void Display() {
        setVisible(true);
    }

    public void Close() {
        setVisible(false);
    }

    public void DrawInGameGUI(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.drawImage(backgroundImage, 305, 347, null);

        graphics.setFont(new Font("Magneto", Font.BOLD, 15));
        graphics.setColor(new Color(0x00, 0x66, 0x00));
        graphics.drawString("Ganja Woodcutter", 264, 365);

        graphics.setFont(new Font("Consolas", Font.PLAIN, 15));
        graphics.setColor(Color.BLACK);
        graphics.drawString("        Run Time:", 280, 382);
        graphics.drawString("Time to level up:", 280, 399);
        graphics.drawString("       XP gained:", 280, 416);
        graphics.drawString("           XP/hr:", 280, 433);

        graphics.drawString("v" + GanjaWoodcutterMain.VERSION, 420, 365);
        graphics.drawImage(ganjaIcon, 470, 347, null);
        graphics.drawString(context.getTimer().formatTime(), 420, 382);
        graphics.drawString(String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(context.getSkillTracker().getTimeToLevel(Skill.WOODCUTTING)),
                TimeUnit.MILLISECONDS.toMinutes(context.getSkillTracker().getTimeToLevel(Skill.WOODCUTTING))
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(context.getSkillTracker().getTimeToLevel(Skill.WOODCUTTING))),
                TimeUnit.MILLISECONDS.toSeconds(context.getSkillTracker().getTimeToLevel(Skill.WOODCUTTING))
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(context.getSkillTracker().getTimeToLevel(Skill.WOODCUTTING)))),
                420, 399);
        graphics.drawString(context.getSkillTracker().getGainedExperience(Skill.WOODCUTTING) + " XP", 420, 416);
        graphics.drawString(context.getSkillTracker().getGainedExperiencePerHour(Skill.WOODCUTTING) + " XP", 420, 433);
    }
}
