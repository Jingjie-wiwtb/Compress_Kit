package hjj;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.awt.Font;
import java.awt.Color;

@SuppressWarnings("serial")
public class Main extends JFrame {


	private JPanel contentPane;
	
	private JTextField src_text;
	private JTextField dest_text;
	private File srcfile;
	private File destpath;
	//private Handle handle;

	
/*
	private JDialog compress_success;
	private JDialog decpress_success;
	private JDialog src_invalid;
	private JDialog src_null;
	
	 */
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main frame = new Main();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Main() {
		
		setTitle("Compress Programm");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 600);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(245, 245, 245));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
    //选择文件		
		JButton choose_srcfile = new JButton("Choose a file");
		choose_srcfile.setFont(new Font("Tekton Pro", Font.PLAIN, 23));
		
		choose_srcfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser jfc_com = new JFileChooser();
				jfc_com.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				jfc_com.showDialog(new JLabel(), "确定");
				srcfile = jfc_com.getSelectedFile();   //选择压缩/被压缩的文件	
			    src_text.setText(srcfile.getAbsolutePath());   //在文本框中显示选择的路径
			}
		});
		choose_srcfile.setBounds(607, 114, 184, 37);
		contentPane.add(choose_srcfile);
		
    //选择路径		
        JButton choose_destpath = new JButton("Choose a folder");
        choose_destpath.setFont(new Font("Tekton Pro", Font.PLAIN, 23));
		
        choose_destpath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				jfc.showDialog(new JLabel(), "确定");
				destpath = jfc.getSelectedFile();   //选择压缩/被压缩的文件
				dest_text.setText(destpath.getAbsolutePath());   //在文本框中显示选择的路径		
			}
		});
        choose_destpath.setBounds(607, 183, 184, 37);
		contentPane.add(choose_destpath);
		
		
	//压缩按钮	
		JButton compress_btn = new JButton("Compress");
		compress_btn.setForeground(new Color(0, 0, 0));
		compress_btn.setFont(new Font("Tekton Pro", Font.PLAIN, 23));
		compress_btn.addMouseListener(new MouseAdapter() {
				
			@Override
			public void mouseClicked(MouseEvent e) {
				
				String srcfileS = src_text.getText();
				String destpathS = dest_text.getText();
				int check = 0 ;
				//————
				if(srcfileS != null && !"".equals(srcfileS)) {  //源文件不为空
					//源文件检查
					srcfile = new File(srcfileS);
					if(!srcfile.exists()) {//不存在
						new Mydialog("<html>The source file does not exit !<br><br> Please choose again!</html>").setVisible(true);
						//"文件不存在！  请重新选择！"
						src_text.setText("");   //清空文本框
						check = 0;
					}
					else {//合法
					    check = 1;
					}
				}
				//未选择文件
				else {					
					new Mydialog("<html>Please choose the source file!</html>").setVisible(true);
					check = 0;
				}
				//————检查目标路径————
			    //选择了目标路径
				if(destpathS != null && !"".equals(destpathS)) {
					destpath = new File(destpathS);
					//路径合法, 且源文件合法
					if(destpath.isDirectory() && check == 1) {						
					    try {
					    	Mydialog processWin = new Mydialog("<html>Compressing... <br>Please wait patiently ~ </html> ");
					    	processWin.setVisible(true);
					    	long startTime = System.currentTimeMillis();					    	
					    	new Handle().compress(srcfileS, destpathS);	
					    	processWin.setVisible(false);
					    	long endTime = System.currentTimeMillis();
					    	long compressTime = endTime - startTime;				    	
							new Mydialog("<html>Compress success!<br> Processing time: "+ compressTime + " ms</html>").setVisible(true);												
						} 
					    catch (Exception err) {
							// TODO Auto-generated catch block
							new Mydialog("Compress failed!").setVisible(true);						
							err.printStackTrace();						
						}
					}
					else{//目标路径不存在
						new Mydialog("<html>The destination path does not exit! <br> Please choose again !</html>").setVisible(true);
						dest_text.setText("");   //清空文本框
					}
				}
				//未选择目标路径——默认
				else {		
					destpathS = srcfileS.substring(0,srcfileS.lastIndexOf(File.separator));
					if(check == 1) {
					    try {
					    	Mydialog processWin = new Mydialog("<html>Compressing... <br>Please wait patiently ~ </html> ");
					    	processWin.setVisible(true);
					    	long startTime = System.currentTimeMillis();					    	
					    	new Handle().compress(srcfileS, destpathS);	
					    	long endTime = System.currentTimeMillis();
					    	long compressTime = endTime - startTime;	
					    	processWin.setVisible(false);	
							new Mydialog("<html>Compress success!<br> Processing time: "+ compressTime +" ms</html>").setVisible(true);						
						} 
					    catch (Exception err) {
							// TODO Auto-generated catch block
							new Mydialog("Compress failed!").setVisible(true);						
							err.printStackTrace();						
						}				
					}
				}				
			}				
		});
		compress_btn.setBounds(115, 333, 252, 37);
		contentPane.add(compress_btn);
	

		
    //解压按钮	
		JButton decompress_btn = new JButton("Decompress");
		decompress_btn.setForeground(new Color(0, 0, 0));
		decompress_btn.setFont(new Font("Tekton Pro", Font.PLAIN, 23));
		
		decompress_btn.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				int check = 0 ;
				String destpathS = dest_text.getText();
				String srcfileS = src_text.getText();
				//源文件检查
				if(srcfileS != null && !"".equals(srcfileS)) {  //源文件不为空
					srcfile = new File(srcfileS);
					String type = srcfileS.substring(srcfileS.indexOf(".") + 1);
					String test = "zip";
					if(!srcfile.exists()) {
						new Mydialog("<html>The source file does not exit ! <br><br> Please choose again!</html>").setVisible(true);
						//"文件不存在！  请重新选择！"
						src_text.setText("");   //清空文本框
						check = 0 ;
					}
					else if(!type.equals(test)){   //源文件类型错误					
						new Mydialog("<html>Sorry,<br> I can only decompress file of type 'zip'</html>").setVisible(true);
						check = 0 ;
					}
					else
						check = 1;
				}
				else {					
					new Mydialog("<html>Please choose the source file!</html>").setVisible(true);		
					check = 0 ;
				}
				
				//检查目标路径
				if(destpathS != null && !"".equals(destpathS)) {//选择了目标路径
					destpath = new File(destpathS);
					//路径合法, 且源文件合法
					if(destpath.isDirectory() && check == 1) {
						
					    try {
					    	Mydialog processWin = new Mydialog("<html>Decompressing... <br>Please wait patiently ~ </html> ");
					    	processWin.setVisible(true);
					    	long startTime = System.currentTimeMillis();					    	
					    	new Handle().decompress(srcfileS, destpathS);					    	
					    	long endTime = System.currentTimeMillis();
					    	long decompressTime = endTime - startTime;	
					    	processWin.setVisible(false);
					    	new Mydialog("<html>Decompress success!<br> Processing time: "+ decompressTime + " ms</html>").setVisible(true);
					    					    							
						} 
					    catch (Exception err) {
							// TODO Auto-generated catch block
							new Mydialog("Decompress failed!").setVisible(true);						
							err.printStackTrace();						
						}
					}
					else{
						new Mydialog("<html>The destination path does not exit! <br> Please choose again !</html>").setVisible(true);
						//"文件不存在！  请重新选择！"
						dest_text.setText("");   //清空文本框
					}
				}
				//未选择文件——默认
				else {		
					destpathS = srcfileS.substring(0,srcfileS.lastIndexOf(File.separator));
					if(check == 1) {
					    try {
					    	Mydialog processWin = new Mydialog("<html>Decompressing... <br>Please wait patiently ~ </html> ");
					    	processWin.setVisible(true);
					    	long startTime = System.currentTimeMillis();					    	
					    	new Handle().decompress(srcfileS, destpathS);	
					    	
					    	long endTime = System.currentTimeMillis();
					    	long decompressTime = endTime - startTime;	
				          	new Mydialog("<html>Decompress success!<br> Processing time: "+ decompressTime +" ms</html>").setVisible(true);
					    	processWin.setVisible(false);					
						} 
					    catch (Exception err) {
							// TODO Auto-generated catch block
							new Mydialog("Decompress failed!").setVisible(true);						
							err.printStackTrace();						
						}				
					}
				}
			}
		});
		
		decompress_btn.setBounds(458, 333, 240, 37);
		contentPane.add(decompress_btn);
		
		
		JLabel srclable = new JLabel("Source File");
		srclable.setFont(new Font("Tekton Pro", Font.PLAIN, 28));
		srclable.setBounds(21, 121, 190, 29);
		contentPane.add(srclable);
		//源文件文本框
		src_text = new JTextField();
		src_text.setFont(new Font("宋体", Font.BOLD, 18));
		src_text.setBounds(181, 114, 391, 35);
		contentPane.add(src_text);
		src_text.setColumns(10);
		
		
		JLabel destlable = new JLabel("Destination");
		destlable.setFont(new Font("Tekton Pro", Font.PLAIN, 28));
		destlable.setBounds(21, 185, 150, 29);
		contentPane.add(destlable);
		//目标文件文本框
		dest_text = new JTextField();
		dest_text.setFont(new Font("宋体", Font.BOLD, 18));
		dest_text.setBounds(181, 185, 391, 35);
		contentPane.add(dest_text);
		dest_text.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Welcome !");
		lblNewLabel.setFont(new Font("Tekton Pro Ext", Font.ITALIC, 28));
		lblNewLabel.setBounds(338, 34, 179, 29);
		contentPane.add(lblNewLabel);
		
		
		JButton exit_btn = new JButton("Exit");
		exit_btn.setFont(new Font("Tekton Pro", Font.PLAIN, 23));
		exit_btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.exit(0);
			}
		});
		exit_btn.setBounds(682, 441, 121, 37);
		contentPane.add(exit_btn);
		
		JLabel lblNewLabel_1 = new JLabel("What do you want to do with it ?");
		lblNewLabel_1.setFont(new Font("Tekton Pro", Font.ITALIC, 28));
		lblNewLabel_1.setBounds(236, 266, 477, 29);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("If you ignore the destination, the default directory will be the same as the source file's.");
		lblNewLabel_2.setFont(new Font("Eras Light ITC", Font.PLAIN, 18));
		lblNewLabel_2.setBounds(84, 406, 669, 29);
		contentPane.add(lblNewLabel_2);
		
		JLabel lblNotice = new JLabel("Notice :");
		lblNotice.setFont(new Font("Eras Light ITC", Font.BOLD | Font.ITALIC, 20));
		lblNotice.setBounds(36, 379, 108, 29);
		contentPane.add(lblNotice);
		
	}
}
