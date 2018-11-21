package garnetyeates;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class GameFrame extends JFrame
{
	private static final long serialVersionUID = 1016170803247561730L;
	
	private JPanel contentPane;
	private JLabel scoreLabel;
	private Snake snakePanel;
	private JLabel canniBuffLabel;
	private JLabel squaresFilledLabel;
	private JLabel speedLabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					GameFrame frame = new GameFrame();
					frame.setVisible(true);
					frame.setResizable(false);	
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	private void createSnake()
	{
	//	Color[] scheme = new Color[] { Color.white, Color.GREEN };
		snakePanel = new Snake(GameFrame.this, 50, 110, 65, 80, Snake.defaultScheme, 3, 6);
		snakePanel.setForeground(Color.BLACK);

		addKeyListener(snakePanel);
		addComponentListener(snakePanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public GameFrame()
	{
		createSnake();
		doWindowBuilderStuff(snakePanel);
		pack();
		this.setSize(new Dimension((int) this.getSize().getWidth(), 178 + snakePanel.preferredHeight()));
	}
	
	public void updateStats()
	{
		DecimalFormat f = new DecimalFormat("#,##0.00");
		scoreLabel.setText(f.format(snakePanel.getProgress() * 100) + "%");
		speedLabel.setText(f.format(snakePanel.getSpeed()) + " squares/sec");
		double canniSeconds = snakePanel.getRemainingCannibalSeconds();
		canniBuffLabel.setText(canniSeconds > 0 ? "Buff: " + (int) canniSeconds : "Buff Inactive");
		squaresFilledLabel.setText((int) snakePanel.getSquaresFilled() + " fruit eaten");
	}

	/**
	 * Code that was auto generated by WindowBuilder, but I've edited it a little bit so that
	 * the size of the snakePanel in this JFrame is equivalent to the size described in
	 * {@link Snake#preferredHeight()} and {@link Snake#preferredWidth()}.
	 * @param snakePanel The snake panel that is in this JFrame
	 */
	public void doWindowBuilderStuff(Snake snakePanel)
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 10, 1088, 272);
		contentPane = new JPanel();
		contentPane.setBackground(Color.BLACK);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		snakePanel.setBorder(new LineBorder(Color.WHITE));
		
		JPanel scareStatsPanelHolder = new JPanel();
		scareStatsPanelHolder.setForeground(Color.BLACK);
		scareStatsPanelHolder.setBackground(Color.BLACK);
		scareStatsPanelHolder.setBorder(new LineBorder(Color.BLACK));
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(snakePanel, GroupLayout.DEFAULT_SIZE, snakePanel.preferredWidth(), Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(267)
					.addComponent(scareStatsPanelHolder, GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
					.addGap(329))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(snakePanel, GroupLayout.PREFERRED_SIZE, snakePanel.preferredHeight() + 1, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scareStatsPanelHolder, GroupLayout.PREFERRED_SIZE, 132, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(80, Short.MAX_VALUE))
		);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBackground(new Color(20, 20, 20));
		panel_2.setBorder(new LineBorder(Color.WHITE));
		
		JLabel statsTitle = new JLabel("Stats");
		statsTitle.setForeground(Color.WHITE);
		statsTitle.setHorizontalAlignment(SwingConstants.CENTER);
		statsTitle.setFont(new Font("Tahoma", Font.BOLD, 18));
		
		canniBuffLabel = new JLabel("0");
		canniBuffLabel.setForeground(Color.WHITE);
		canniBuffLabel.setHorizontalAlignment(SwingConstants.CENTER);
		canniBuffLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		
		speedLabel = new JLabel("");
		speedLabel.setHorizontalAlignment(SwingConstants.CENTER);
		speedLabel.setForeground(Color.WHITE);
		speedLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
						.addComponent(canniBuffLabel, GroupLayout.PREFERRED_SIZE, 189, GroupLayout.PREFERRED_SIZE)
						.addComponent(statsTitle, GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
						.addComponent(speedLabel, GroupLayout.PREFERRED_SIZE, 189, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		gl_panel_2.setVerticalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addComponent(statsTitle, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(canniBuffLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(speedLabel, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(19, Short.MAX_VALUE))
		);
		panel_2.setLayout(gl_panel_2);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(20, 20, 20));
		panel_1.setBorder(new LineBorder(Color.WHITE));
		
		JLabel scoreTitle = new JLabel("Score");
		scoreTitle.setForeground(Color.WHITE);
		scoreTitle.setFont(new Font("Tahoma", Font.BOLD, 18));
		scoreTitle.setHorizontalAlignment(SwingConstants.CENTER);
		
		scoreLabel = new JLabel("0");
		scoreLabel.setForeground(Color.WHITE);
		scoreLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		squaresFilledLabel = new JLabel("0");
		squaresFilledLabel.setHorizontalAlignment(SwingConstants.CENTER);
		squaresFilledLabel.setForeground(Color.WHITE);
		squaresFilledLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addComponent(scoreLabel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
						.addComponent(scoreTitle, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
						.addComponent(squaresFilledLabel, GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addComponent(scoreTitle, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scoreLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(squaresFilledLabel, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(19, Short.MAX_VALUE))
		);
		panel_1.setLayout(gl_panel_1);
		GroupLayout gl_scareStatsPanelHolder = new GroupLayout(scareStatsPanelHolder);
		gl_scareStatsPanelHolder.setHorizontalGroup(
			gl_scareStatsPanelHolder.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_scareStatsPanelHolder.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(12))
		);
		gl_scareStatsPanelHolder.setVerticalGroup(
			gl_scareStatsPanelHolder.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_scareStatsPanelHolder.createSequentialGroup()
					.addGroup(gl_scareStatsPanelHolder.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 119, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 119, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		scareStatsPanelHolder.setLayout(gl_scareStatsPanelHolder);
		contentPane.setLayout(gl_contentPane);	
	}
}
