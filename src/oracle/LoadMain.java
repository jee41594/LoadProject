/*
����

<�뷮�� ������ ���� ��>
�� ����Ŭ���� insert�Ϸ��� insert ���� ��
�Ϻ�/�̱��� db�� �ֱ⶧����
while���� �� ������ -> �Ϻθ� ���鼭 �����Ǳ⶧����

Thread - sleep���� �̿�! �� 0.5��..?
-----------------------------------------------------------
<���� ��������>
����ġ���� Apache POI - the Java API for Microsoft Documents
http://poi.apache.org/apidocs/index.html  ���� API����

�÷�+ �ο� -> SHEET -> ����
Class HSSFWorkbook = EXCEL ����!



 */
package oracle;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;

public class LoadMain extends JFrame implements ActionListener{
	
	JPanel p_north;
	JTextField t_path;
	JButton bt_open, bt_load, bt_excel, bt_del;
	JTable table;
	JScrollPane scroll;
	JFileChooser chooser;
	FileReader reader=null; //reader�� ������ ������ ���ڱ�� ��Ʈ������ �Է½�Ʈ��
	//���پ� �а�����ϱ� ���׷��̵�
	BufferedReader buffr =null;
	
	 //������ â�� ������ �̹� db�� ������ Ȯ���ؾ� �Ѵ�.
	DBManager manager = DBManager.getInstance();
	Connection con;
	
	public LoadMain() {
		p_north = new JPanel();
		t_path = new JTextField(25);
		bt_open = new JButton("���Ͽ���");
		bt_load = new JButton("�ε��ϱ�");
		bt_excel = new JButton("�����ε�");
		bt_del = new JButton("�����ϱ�");
		
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
		
		bt_open.addActionListener(this);
		bt_load.addActionListener(this);
		bt_excel.addActionListener(this);
		bt_del.addActionListener(this);
		
		//������� Listener�� ����
		this.addWindowListener(new WindowAdapter() {
			
			public void windowClosing(WindowEvent e) {
				//������â�� ������ db �ڿ�����
				manager.disConnect(con);
				//���μ��� ����
				System.exit(0);
			}
		});
		
		setVisible(true);
		setSize(800, 600);
		//setDefaultCloseOperation(EXIT_ON_CLOSE);
		//���߿� DB������ ���ϴ� ��Ȳ���� �ݱ� ���� ���߿��� SETDefault�A��.
		
		//db ���� ���� ���� ������ �������� init ������
		init();
	}
	
	public void init() {
		//Connection ���� ����!! ������ ����ڸ��� Ȯ�� ����
		con = manager.getConncection();
	}
	
	/*
	 ���� ���� �о db�� ���̱׷��̼� �ϱ�
	javaSE �������� ���̺귯�� �ִ�? X! ����!!
	open Source ��������Ʈ����
	copyright(����Ʈ���� ������!) <--> copyleft(����ġ ��ü : ����Ʈ����� ����ȭ)
	POI ���̺귯�� http://apache.org
	 
	 HSSFWorkbook : ���� ����
	 Class HSSFSheet : Sheet
	 HSSFRow: row
	 HSSFCell : cell
	 
	 HSSFRow row = sheet.getRow(0);
	 System.out.println(cell.getStringCellValue());
	 */
	
	public void loadExcel() {
		int result = chooser.showOpenDialog(this);
		StringBuffer sb_cell = new StringBuffer();
		String data;
		
		if(result==JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			//fileŬ�������� String���� ����� ���
			FileInputStream fis = null;
			
			try {
				//���� ������ ���� �ȱ� �ٵ� �� ������ �ν��Ҽ��� �����Ƿ�
				//�̸� ������ �� �ִ� ��ü�� �ʿ�! HssfWorkbook!
				fis = new FileInputStream(file);
				
				HSSFWorkbook book = null;
				book = new HSSFWorkbook(fis); //���������� �����ܰ�! -> sheet�����ؾ���!
				
				HSSFSheet sheet=null;
				sheet = book.getSheet("��������");
				
				int total = sheet.getLastRowNum();
				DataFormatter df = new DataFormatter(); //�긦 �̿��� ����

				for(int a=1; a<total; a++){
					HSSFRow row = sheet.getRow(a);
					int columnCount = row.getLastCellNum();
					
					for(int i =0; i<columnCount; i++) {
						HSSFCell cell = row.getCell(i);
						
						//�ڷ���(���� or ����)�� ���ѵ��� �ʰ� ��� Stringó���ؾ��Ѵ�. DataFormatter ���!
						String value = df.formatCellValue(cell);
						System.out.print(value);
					}
					System.out.println("");
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	// ������ ���ڵ� ����
	public void delete() {
		
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

	// ���� Ž���� ����
	public void open() {
		int result = chooser.showOpenDialog(this);
		
		//���⸦ ������ 
		//���� ���Ͽ� ��Ʈ���� ��������
		if(result == JFileChooser.APPROVE_OPTION){
			
			File file = chooser.getSelectedFile(); //������ ������ ����
			t_path.setText(file.getAbsolutePath()); //�������� ��� �Ѹ���
			
			try {
				//user�� �����츦 ���� �������� csv�����ϰ� ok ������
				//���ϰ� ������ �Ǿ��ִ� ���� = ������ �Ȱ� ������ x
				//���� ����� �̰� �� �����ϸ� ������, ���� ������� ���̶�°� ��� �� ����!
				//������ load������ �б����!
				reader = new FileReader(file);
				buffr = new BufferedReader(reader); //reader�� ��Ƹ��� ���׷��̵�!		

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} 
		}
	}
	
	// CSV --> Oracle�� ������ migration
	public void load() {
		//�ö���ִ� buffr�� null�� �ƴ��� Ȯ������
		//System.out.println(buffr);
		
		/*-----------------------------------------------------------------
		 ���۽�Ʈ���� �̿��Ͽ� csv�� �����͸� 1�پ� �о�鿩 insert ��Ű��
		 ���ڵ尡 ���� �� ����!
		 but, while������ ������ �ʹ� �ӵ��� �����Ƿ� ��Ʈ��ũ�� ������ �� ����.
		 �Ϻ귯 ������Ű�鼭 insert����
		 -----------------------------------------------------------------*/
		
		String data;
		StringBuffer sb = new StringBuffer(); //String������ �޸����̰� ŭ!
		
		PreparedStatement pstmt = null;
		
		//IOException ���⼭ �߻�!
		try {
			while(true) {
				data = buffr.readLine();
				
				if(data==null)break; //data�� ���ٸ� ����������
				/*
				 �÷� seq, ���� �� �����ؾ���
				 ,�� �������� �и���Ű�� -> ���������� String������ seq, name ���� �и��ȴ�. -> �迭!
				 ,�� Ư�����ڷ� ���� x ���ڷ� ����!
				 */
				String[] value = data.split(",");
				//seq�� �߰ߵ��� �ʴ´ٸ� ���� insert�Ҳ��� !(����)�� �־�����
				if(!value[0].equals("seq")){
					//load�ϱ� �ϸ� ��µ��� �ʴ´� seq�������Ƿ�,, -> while�� �߰�
					//String sql �� ���� �޸� ��û ��ƸԴ´�.
					sb.append("insert into hospital(seq,name,addr,regdate,status,dimension,type)");
					sb.append(" values("+value[0]+",'"+value[1]+"','"+value[2]+"','"+value[3]+"','"+value[4]+"',"+value[5]+",'"+value[6]+"')");
					
					System.out.println(sb.toString());
					//pstmt���Ե� ������ �˷�����Ѵ�.
					pstmt = con.prepareStatement(sb.toString());
					
					int result = pstmt.executeUpdate();
					
					//insert������ apeend ���������� StringBuffer�� ��� �����ǹǷ�
					sb.delete(0, sb.length());
					
				} else{
					System.out.println("������");
				}
			} 
			JOptionPane.showMessageDialog(this, "���̱׷��̼� �Ϸ�!");
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}		
		}	
	}
	

	public static void main(String[] args) {
		new LoadMain();
	}

}
