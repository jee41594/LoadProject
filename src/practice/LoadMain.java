package practice;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class LoadMain extends JFrame implements ActionListener{

	JPanel p_north;
	JTextField t_path;
	JButton bt_open, bt_load, bt_excel, bt_del;
	JTable table;
	JScrollPane scroll;
	JFileChooser chooser;
	FileReader reader=null;
	BufferedReader buffr  = null;
	
	public LoadMain() {
		
		p_north = new JPanel();
		t_path = new JTextField(25);
		bt_open = new JButton("파일열기");
		bt_load = new JButton("로드하기");
		bt_excel = new JButton("엑셀로드");
		bt_del = new JButton("삭제하기");
		
		//JTable이 안나오는 이유는 현재 언제 나올지 지정x!
		table=  new JTable();
		scroll = new JScrollPane(table);
		chooser = new JFileChooser("C:/animal");
		
		p_north.add(t_path);
		p_north.add(bt_open);
		p_north.add(bt_load);
		p_north.add(bt_excel);
		p_north.add(bt_del);
		
		add(p_north, BorderLayout.NORTH);
		add(scroll);
		
		setVisible(true);
		setSize(800, 600);
		//setDefaultCloseOperation(EXIT_ON_CLOSE);
		//나중에 DB연동을 원하는 상황에서 닫기 위해 나중에는 SETDefault뺸다.
		
		//db 연결 등을 위해 생성자 마지막에 init 만들자
		init();
		
		bt_open.addActionListener(this);
		bt_load.addActionListener(this);
		bt_excel.addActionListener(this);
		bt_del.addActionListener(this);
		

		
		this.addWindowListener(new WindowAdapter() {
			
			public void windowClosing(WindowEvent e) {
			}
		});
			

	}
	
	public void init() {
		
	}
	
	public void getList() {
		String sql = "select * from hospital order by seq asc";
		
	}

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if(obj==bt_open) {
			open();
		} else if(obj ==bt_load) {
			load();
		} else if(obj==bt_excel) {
			loadExcel();			
		} else if(obj==bt_del) {
			delete();			
		}
	}
	
	public void open() {
		int result = chooser.showOpenDialog(this);
		if(result == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			String path = file.getAbsolutePath();
			t_path.setText(path);
			
			try {
				reader = new FileReader(file);
				buffr = new BufferedReader(reader);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}	
	}
	
	public void load() {
		
	}
	
	public void loadExcel() {
		
	}
	
	public void delete() {
		
	}

	public static void main(String[] args) {
		
		new LoadMain();
		
		
	}


}
