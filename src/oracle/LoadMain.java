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

TableModel ����ϸ� ������ ������ TableModel�� å�������Ѵ�
JTable(3,8) �ϸ� ��������������
TableModel �� �Ұ���!

------------------------------------------------------------
<����>
�����ϰ� enterġ�� ������ ��������!
------------------------------------------------------------
���� ������ ���н�Ű��


 */
package oracle;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;

import util.file.FileUtil;

public class LoadMain extends JFrame implements ActionListener, TableModelListener, Runnable{
	
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
	Vector<Vector> list;
	Vector columnName;
	//MyModel myModel;
	Thread thread;
	/*
 	���� ��Ͻ� ���� ������ 
	 �� ���?
	 ������ ���� �ʹ� ���� ���, Ȥ�� ��Ʈ��ũ ���°� ���� ���� ���
	 insert�� while�� �ӵ��� �����󰣴�.
	 ���� �������� ���� �Ϻη� �ð� ������ ������ insert �õ��Ұ���
	 */
	
	//���� ���Ͽ� ���� ������ �������� �����尡 ����� �� �ִ� ���·� �����س���!!
	StringBuffer insertSql = new StringBuffer();
	String seq;
	
	public LoadMain() {
		p_north = new JPanel();
		t_path = new JTextField(25);
		bt_open = new JButton("���Ͽ���");
		bt_load = new JButton("csv �ε��ϱ�");
		bt_excel = new JButton("exl �����ε�");
		bt_del = new JButton("�����ϱ�");
		
		//JTable�� �ȳ����� ������ ���� ���� ������ ����x!
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
		
		//Listener�ֱ⿣ �ʹ� �����ϱ� Adapter�����͸����� ����!
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JTable t = (JTable)e.getSource();
				
				int col = 0; //seq�� ù��° �÷��̴ϱ� �׻� 0
				int row = t.getSelectedRow();
				seq = (String)t.getValueAt(row, col);
			}
		});
		
		//������� Listener�� ����
		this.addWindowListener(new WindowAdapter() {
			
			public void windowClosing(WindowEvent e) {
				//������â�� ������ db �ڿ�����
				manager.disConnect(con);
				//���μ��� ����
				System.exit(0);
			}
		});
		
		//TableModel�� Listener���� ����
		//JTable�� �ڱⰡ ����ϰ� �ִ� model��ȯ���ش�.
		//table.getModet()�ϸ�ȴ�.
		//table.getModel().addTableModelListener(this);
		//���� ���� ������ ���⼭�� �ȵȴ�

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
		
		StringBuffer cols = new StringBuffer(); //�÷� ��Ƴ��� ��Ʈ������
		StringBuffer data = new StringBuffer(); //value�� ��Ƴ��� ��Ʈ������

		int result = chooser.showOpenDialog(this);
		
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
				/*
				 ù��° row�� �����Ͱ� �ƴ� �÷� �����̹Ƿ�, �� �������� �����Ͽ�
				 insert into table(****) �� ���ʿ� ����.
				 */

				System.out.println("�� ������ ù��° row ��ȣ��"+sheet.getFirstRowNum()); 
				//�� ������ ù��° row ��ȣ��0�̶� ��
				HSSFRow firstRow = sheet.getRow(sheet.getFirstRowNum());
				
				//��Ʈ������ ����! ��� �׿��� �������Ƿ�
				cols.delete(0, cols.length());
				
				//Row�� ������� �÷��� �м��Ѵ�.
				//firstRow.getLastCellNum(); //������ cell��ȣ ������
				for(int i=0; i<firstRow.getLastCellNum(); i++) {
					HSSFCell cell = firstRow.getCell(i);
					
					//������ ��ǥ�� ������ �ʵ��� �Ϸ��� 2���� ��찡 ������ �˾ƾ� �Ѵ�.
					if(i< firstRow.getLastCellNum()-1) {
						//System.out.print(cell.getStringCellValue()+",");
						//System.out.print(cell+",");
						cols.append(cell.getStringCellValue()+",");
					} else{
						//System.out.print(cell.getStringCellValue());
						//StringBuffer�� cols�� ��Ƽ� ����. ����� ����� �������� sysout�Ѱ��� ������ x �ǹǷ�! cols�̿�!
						cols.append(cell.getStringCellValue());
					} 
				}
				
				DataFormatter df = new DataFormatter(); //�긦 �̿��� ����
				
				for(int a=1; a<total; a++){
					HSSFRow row = sheet.getRow(a);
					
					int columnCount = row.getLastCellNum();
					
					data.delete(0, data.length()); //���⵵ ����ֱ�
					
					for(int i =0; i<columnCount; i++) {
						HSSFCell cell = row.getCell(i);
						
						//�ڷ���(���� or ����)�� ���ѵ��� �ʰ� ��� Stringó���ؾ��Ѵ�. DataFormatter ���!
						String value = df.formatCellValue(cell);
						//System.out.print(value);
						//''ȣ����ǥ�� ����.
						if(cell.getCellType()==HSSFCell.CELL_TYPE_STRING) {
							value="'"+value+"'";						
						}
						
						//�̰͵� �޸� 2���� ���� !
						if(i<columnCount-1) {
							data.append(value+",");
						} else{
							data.append(value);
						}
					}
					System.out.println("insert into hospital("+cols.toString()+") values("+data.toString()+")");
					//��»Ӹ� �ƴ϶� StringBuffer���� ��Ƴ��� �Ѵ�.
					insertSql.append("insert into hospital("+cols.toString()+") values("+data.toString()+");");
				}
				
				//���� �������� ����Ѱ� �����忡 �� ��Ű��!!
				//Runnable �������̽��� �μ��� ������ Thread�� run�� �����ϴ� ���� �ƴ϶�
				//Runnable �������̽��� ������ ���� run�� �����ϴ� ��
				//���� �츮 �� ���� = this
				thread = new Thread(this);
				thread.start();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	
	//��� ���ڵ� �������� -> JTable��
	public void getList() {
		String sql = "select*from hospital order by seq asc";
		
		PreparedStatement pstmt = null;
		ResultSet rs= null;
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			//table�� mvc�̱� ������ model�� ���� �־���ϰ� rs�� model�� �����ؾ� �Ѵ�.
			//rs�� 2���� Vector�� �����ؾ� �Ѵ�.
			
			ResultSetMetaData meta = rs.getMetaData(); //columnName�� �޾ƿ��� rs�ױ�����
			int count = meta.getColumnCount();
			columnName = new Vector<>();
			
			//for�� ������ column���� vector�� add�ϸ� �ȴ�.
			for(int i=0; i<count; i++){
				columnName.add(meta.getColumnName(i+1));				
			}
			
			list = new Vector<Vector>(); //ūvector�� 2�������Ͱ� �� ����!
			
			while(rs.next()) {
				Vector vec = new Vector(); //���ڵ� �Ѱ� ���			
				vec.add(rs.getString("seq"));
				vec.add(rs.getString("name"));
				vec.add(rs.getString("addr"));
				vec.add(rs.getString("regdate"));
				vec.add(rs.getString("status"));
				vec.add(rs.getString("dimension"));
				vec.add(rs.getString("type"));
				
				list.add(vec);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}				
			}
		}

	}
 /*
	public void getList() {
		String sql = "select * from hospital order by sec asc";
		PreparedStatement pstmt = null;
		ResultSet rs = null;	
		
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();

			while(rs.next()){
				Vector vec = new Vector();
				vec.add(rs.getString("seq"));
				vec.add(rs.getString("name"));
				vec.add(rs.getString("addr"));
				vec.add(rs.getString("regdate"));
				vec.add(rs.getString("status"));
				vec.add(rs.getString("dimension"));
				vec.add(rs.getString("type"));	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}*/
	
	// ������ ���ڵ� ����
	public void delete() {
		//yes or no�� �˷��� ����� �޾ƾ� �Ѵ�.
		int ans = JOptionPane.showConfirmDialog(LoadMain.this, seq+ "�����ҷ���?");
		if(ans == JOptionPane.OK_OPTION) {
			String sql = "delete from hospital where seq="+seq;
			
			PreparedStatement pstmt=null;
			try {
				pstmt = con.prepareStatement(sql);
				int result = pstmt.executeUpdate();
				if(result!=0) {
					JOptionPane.showMessageDialog(this, "�����Ϸ�");
					table.updateUI();
				}
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
			String ext = FileUtil.getExt(file.getName());
			
			if(!ext.equals("csv")){
				JOptionPane.showMessageDialog(this, "CSV�� �����ϼ���");
				return; //���̻��� ������ ���´�
			}
				//csv���� ���ϸ� ���� ��������			
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
			//JTable ������ ó��!!
			getList();
			//�ٽ� �� �׷���
			table.setModel(new MyModel(list, columnName));
			//�� ������ ������ getModel�������
			table.getModel().addTableModelListener(this);
			
			//���̺� ƨ���ָ鼭 �ֽ� ������ �ݿ��ǵ���
			table.updateUI();	
			
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
	
	//���̺� ���� �����Ͱ��� ������ �߻��ϸ�
	//�� ������ �����ϴ� ������!!
	public void tableChanged(TableModelEvent e) {
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();
		
		
		
		String column = (String)columnName.elementAt(col);
		String value = (String)table.getValueAt(row, col);
		String seq = (String)table.getValueAt(row, 0);
		
		System.out.println(row);
		String sql = "update hospital set " + column +"='"+value+"'";
		//���� ��� �ֵ� �� �տ� �ִ°� seq�̴�.
		sql +=" where seq="+seq;
		//�������� �� �˾Ƹ���� �Ѵ�. e����Ѵ�.
		//System.out.println(sql);
		PreparedStatement pstmt=null;
		
		try {
			pstmt = con.prepareStatement(sql);
			int result = pstmt.executeUpdate();
			if(result!=0) {
				JOptionPane.showMessageDialog(this, "�����Ϸ�");
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}				
			}
		}
		
	}
	

	public void run() {
		//insertSql�� insert ���� ��ִ��� �˾ƺ���!
		String[] str = insertSql.toString().split(";");
		System.out.println("insert�� ����"+str.length);
		
		PreparedStatement pstmt =null;
		
		for(int i =0; i<str.length; i++) {
			//System.out.println(str[i]);	
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//try catch ������ ���� ��Ҵ�� insert�� ������ �ȴ�.
			//���������� 1��1 ��Ī�Ǽ� �ö��
			try {
				pstmt = con.prepareStatement(str[i]);
				int result = pstmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		//������ ����ߴ� StringBuffer ����
		insertSql.delete(0, insertSql.length());
		
		if(pstmt!=null) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
		//��� INSERT�� ����Ǹ� JTable UI����
	}

	public static void main(String[] args) {
		new LoadMain();
	}
}
