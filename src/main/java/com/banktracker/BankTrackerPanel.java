package com.banktracker;

import net.runelite.client.ui.PluginPanel;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class BankTrackerPanel extends PluginPanel
{
    private final JButton refreshButton = new JButton("Refresh");

    public BankTrackerPanel(BankTrackerPlugin bankTrackerPlugin)
    {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Bank Tracker");
        add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.add(refreshButton);

        add(content, BorderLayout.CENTER);
    }

    public JButton getRefreshButton()
    {
        return refreshButton;
    }
}