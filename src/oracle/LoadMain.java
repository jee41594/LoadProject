/*
민쿡

<대량의 데이터 넣을 때>
이 오라클ㅇ로 insert하려면 insert 넣을 때
일본/미국의 db로 넣기때문에
while문이 더 빠르다 -> 일부만 들어가면서 버벅되기때문에

Thread - sleep으로 이용! 한 0.5초..?
-----------------------------------------------------------
<엑셀 가져오기>
아파치에서 Apache POI - the Java API for Microsoft Documents
http://poi.apache.org/apidocs/index.html  에서 API보자

컬럼+ 로우 -> SHEET -> 파일
Class HSSFWorkbook = EXCEL 파일!

TableModel 사용하면 편집의 유무를 TableModel이 책임져야한다
JTable(3,8) 하면 수정가능하지만
TableModel 은 불가능!

------------------------------------------------------------
<수정>
수정하고 enter치면 쿼리문 보내도록!
------------------------------------------------------------
선택 파일을 구분시키는


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
	FileReader reader=null; //reader로 끝났기 때문에 문자기반 스트림이자 입력스트림
	//한줄씩 읽고싶으니까 업그레이드
	BufferedReader buffr =null;
	
	 //윈도우 창이 열리면 이미 db가 접속을 확보해야 한다.
	DBManager manager = DBManager.getInstance();
	Connection con;
	Vector<Vector> list;
	Vector columnName;
	//MyModel myModel;
	Thread thread;
	/*
 	엑셀 등록시 사용될 쓰레드 
	 왜 사용?
	 데이터 량의 너무 많을 경우, 혹은 네트워크 상태가 좋지 않을 경우
	 insert가 while문 속도를 못따라간다.
	 따라서 안정성을 위해 일부러 시간 지연을 일으켜 insert 시도할거임
	 */
	
	//엑셀 파일에 의해 생성된 쿼리문을 쓰레드가 사용할 수 있는 상태로 저장해놓자!!
	StringBuffer insertSql = new StringBuffer();
	String seq;
	
	public LoadMain() {
		p_north = new JPanel();
		t_path = new JTextField(25);
		bt_open = new JButton("파일열기");
		bt_load = new JButton("csv 로드하기");
		bt_excel = new JButton("exl 엑셀로드");
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
		
		bt_open.addActionListener(this);
		bt_load.addActionListener(this);
		bt_excel.addActionListener(this);
		bt_del.addActionListener(this);
		
		//Listener주기엔 너무 많으니까 Adapter내부익명으로 하자!
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JTable t = (JTable)e.getSource();
				
				int col = 0; //seq는 첫번째 컬럼이니까 항상 0
				int row = t.getSelectedRow();
				seq = (String)t.getValueAt(row, col);
			}
		});
		
		//윈도우와 Listener와 연결
		this.addWindowListener(new WindowAdapter() {
			
			public void windowClosing(WindowEvent e) {
				//윈도우창이 닫히면 db 자원해제
				manager.disConnect(con);
				//프로세스 종료
				System.exit(0);
			}
		});
		
		//TableModel과 Listener와의 연결
		//JTable은 자기가 사용하고 있는 model반환해준다.
		//table.getModet()하면된다.
		//table.getModel().addTableModelListener(this);
		//여기 시점 문제로 여기서는 안된다

		setVisible(true);
		setSize(800, 600);
		//setDefaultCloseOperation(EXIT_ON_CLOSE);
		//나중에 DB연동을 원하는 상황에서 닫기 위해 나중에는 SETDefault뺸다.
		
		//db 연결 등을 위해 생성자 마지막에 init 만들자
		init();
	}
	
	public void init() {
		//Connection 얻어다 놓기!! 윈도우 띄우자마자 확보 가능
		con = manager.getConncection();
	}
	
	/*
	 엑셀 파일 읽어서 db에 마이그레이션 하기
	javaSE 엑셀제어 라이브러리 있다? X! 없다!!
	open Source 공개소프트웨어
	copyright(소프트웨어 돈내라!) <--> copyleft(아파치 단체 : 소프트웨어는 무료화)
	POI 라이브러리 http://apache.org
	 
	 HSSFWorkbook : 엑셀 파일
	 Class HSSFSheet : Sheet
	 HSSFRow: row
	 HSSFCell : cell
	 
	 HSSFRow row = sheet.getRow(0);
	 System.out.println(cell.getStringCellValue());
	 */
	
	public void loadExcel() {
		
		StringBuffer cols = new StringBuffer(); //컬럼 모아놓는 스트링버퍼
		StringBuffer data = new StringBuffer(); //value들 모아놓는 스트링버퍼

		int result = chooser.showOpenDialog(this);
		
		if(result==JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			//file클래스에서 String으로 만드는 방법
			FileInputStream fis = null;
			
			try {
				//현재 엑셀에 빨대 꽂기 근데 이 엑셀을 인식할수가 없으므로
				//이를 제어할 수 있는 객체가 필요! HssfWorkbook!
				fis = new FileInputStream(file);
				
				HSSFWorkbook book = null;
				book = new HSSFWorkbook(fis); //엑셀파일이 열린단계! -> sheet접근해야함!
				
				HSSFSheet sheet=null;
				sheet = book.getSheet("동물병원");
				
				int total = sheet.getLastRowNum();
				/*
				 첫번째 row는 데이터가 아닌 컬럼 정보이므로, 이 정보들을 추출하여
				 insert into table(****) 이 안쪽에 넣자.
				 */

				System.out.println("이 파일의 첫번째 row 번호는"+sheet.getFirstRowNum()); 
				//이 파일의 첫번째 row 번호는0이라 뜸
				HSSFRow firstRow = sheet.getRow(sheet.getFirstRowNum());
				
				//스트링버퍼 비우기! 계속 쌓여서 많아지므로
				cols.delete(0, cols.length());
				
				//Row를 얻었으니 컬럼을 분석한다.
				//firstRow.getLastCellNum(); //마지막 cell번호 얻어오기
				for(int i=0; i<firstRow.getLastCellNum(); i++) {
					HSSFCell cell = firstRow.getCell(i);
					
					//마지막 쉼표가 찍히지 않도록 하려면 2가지 경우가 있음을 알아야 한다.
					if(i< firstRow.getLastCellNum()-1) {
						//System.out.print(cell.getStringCellValue()+",");
						//System.out.print(cell+",");
						cols.append(cell.getStringCellValue()+",");
					} else{
						//System.out.print(cell.getStringCellValue());
						//StringBuffer인 cols에 모아서 쓴다. 현재는 디버깅 목적으로 sysout한거지 보관은 x 되므로! cols이용!
						cols.append(cell.getStringCellValue());
					} 
				}
				
				DataFormatter df = new DataFormatter(); //얘를 이용해 가공
				
				for(int a=1; a<total; a++){
					HSSFRow row = sheet.getRow(a);
					
					int columnCount = row.getLastCellNum();
					
					data.delete(0, data.length()); //여기도 비워주기
					
					for(int i =0; i<columnCount; i++) {
						HSSFCell cell = row.getCell(i);
						
						//자료형(숫자 or 문자)에 국한되지 않고 모두 String처리해야한다. DataFormatter 사용!
						String value = df.formatCellValue(cell);
						//System.out.print(value);
						//''호따옴표가 없다.
						if(cell.getCellType()==HSSFCell.CELL_TYPE_STRING) {
							value="'"+value+"'";						
						}
						
						//이것도 콤마 2가지 경우로 !
						if(i<columnCount-1) {
							data.append(value+",");
						} else{
							data.append(value);
						}
					}
					System.out.println("insert into hospital("+cols.toString()+") values("+data.toString()+")");
					//출력뿐만 아니라 StringBuffer에도 담아놔야 한다.
					insertSql.append("insert into hospital("+cols.toString()+") values("+data.toString()+");");
				}
				
				//모든게 끝났으니 편안한게 쓰레드에 일 시키자!!
				//Runnable 인터페이스를 인수로 넣으면 Thread의 run을 수행하는 것이 아니라
				//Runnable 인터페이스를 구현한 자의 run을 수행하는 것
				//따라서 우리 것 수행 = this
				thread = new Thread(this);
				thread.start();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	
	//모든 레코드 가져오기 -> JTable에
	public void getList() {
		String sql = "select*from hospital order by seq asc";
		
		PreparedStatement pstmt = null;
		ResultSet rs= null;
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			//table은 mvc이기 때문에 model을 통해 넣어야하고 rs랑 model을 가공해야 한다.
			//rs는 2차원 Vector로 가공해야 한다.
			
			ResultSetMetaData meta = rs.getMetaData(); //columnName을 받아오자 rs죽기전에
			int count = meta.getColumnCount();
			columnName = new Vector<>();
			
			//for문 돌려서 column들을 vector에 add하면 된다.
			for(int i=0; i<count; i++){
				columnName.add(meta.getColumnName(i+1));				
			}
			
			list = new Vector<Vector>(); //큰vector로 2차원백터가 될 예정!
			
			while(rs.next()) {
				Vector vec = new Vector(); //레코드 한건 담기			
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
	
	// 선택한 레코드 삭제
	public void delete() {
		//yes or no을 알려면 상수로 받아야 한다.
		int ans = JOptionPane.showConfirmDialog(LoadMain.this, seq+ "삭제할래요?");
		if(ans == JOptionPane.OK_OPTION) {
			String sql = "delete from hospital where seq="+seq;
			
			PreparedStatement pstmt=null;
			try {
				pstmt = con.prepareStatement(sql);
				int result = pstmt.executeUpdate();
				if(result!=0) {
					JOptionPane.showMessageDialog(this, "삭제완료");
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

	// 파일 탐색기 띄우기
	public void open() {
		int result = chooser.showOpenDialog(this);
		
		//열기를 누르면 
		//목적 파일에 스트림을 생성하자
		if(result == JFileChooser.APPROVE_OPTION){

			File file = chooser.getSelectedFile(); //유저가 선택한 파일
			String ext = FileUtil.getExt(file.getName());
			
			if(!ext.equals("csv")){
				JOptionPane.showMessageDialog(this, "CSV만 선택하세요");
				return; //더이상의 진행을 막는다
			}
				//csv선택 안하면 욕이 나오도록			
				t_path.setText(file.getAbsolutePath()); //선택파일 경로 뿌리기
				
			try {
				//user가 윈도우를 띄우고 동물병원 csv선택하고 ok 누르면
				//파일과 연결이 되어있는 상태 = 빨때만 꽂고 흡입은 x
				//옆에 사람이 이걸 또 쓰려하면 쓰기중, 문서 열어놓은 중이라는거 띄울 수 있음!
				//다음에 load누르면 읽기시작!
				reader = new FileReader(file);
				buffr = new BufferedReader(reader); //reader를 잡아먹음 업그레이드!		

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	// CSV --> Oracle로 데이터 migration
	public void load() {
		//올라와있는 buffr이 null이 아님을 확인하자
		//System.out.println(buffr);
		
		/*-----------------------------------------------------------------
		 버퍼스트림을 이용하여 csv의 데이터를 1줄씩 읽어들여 insert 시키자
		 레코드가 없을 때 까지!
		 but, while문으로 돌리면 너무 속도가 빠르므로 네트워크가 감당할 수 없다.
		 일브러 지연시키면서 insert하자
		 -----------------------------------------------------------------*/
		
		String data;
		StringBuffer sb = new StringBuffer(); //String쓸때랑 메모리차이가 큼!
		
		PreparedStatement pstmt = null;
		
		//IOException 여기서 발생!
		try {
			while(true) {
				data = buffr.readLine();
				
				if(data==null)break; //data가 없다면 빠져나오자
				/*
				 컬럼 seq, 등은 은 제외해야함
				 ,를 기준으로 분리시키자 -> 동물병원은 String형으로 seq, name 각각 분리된다. -> 배열!
				 ,는 특수문자로 구분 x 문자로 구분!
				 */
				String[] value = data.split(",");
				//seq가 발견되지 않는다면 나는 insert할꺼야 !(부정)을 넣어주자
				if(!value[0].equals("seq")){
					//load하기 하면 출력되지 않는다 seq만났으므로,, -> while문 추가
					//String sql 로 가면 메모리 엄청 잡아먹는다.
					sb.append("insert into hospital(seq,name,addr,regdate,status,dimension,type)");
					sb.append(" values("+value[0]+",'"+value[1]+"','"+value[2]+"','"+value[3]+"','"+value[4]+"',"+value[5]+",'"+value[6]+"')");
					
					System.out.println(sb.toString());
					//pstmt에게도 쿼리문 알려줘야한다.
					pstmt = con.prepareStatement(sb.toString());
					
					int result = pstmt.executeUpdate();
					
					//insert끝나면 apeend 취소해줘야함 StringBuffer가 계속 누적되므로
					sb.delete(0, sb.length());
					
				} else{
					System.out.println("난제외");
				}
			}
			JOptionPane.showMessageDialog(this, "마이그레이션 완료!");
			//JTable 나오게 처리!!
			getList();
			//다시 너 그려라
			table.setModel(new MyModel(list, columnName));
			//모델 적용한 다음에 getModel해줘야함
			table.getModel().addTableModelListener(this);
			
			//테이블 튕겨주면서 최신 데이터 반영되도록
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
	
	//테이블 모델의 데이터값에 변경이 발생하면
	//그 찰나를 감지하는 리스너!!
	public void tableChanged(TableModelEvent e) {
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();
		
		
		
		String column = (String)columnName.elementAt(col);
		String value = (String)table.getValueAt(row, col);
		String seq = (String)table.getValueAt(row, 0);
		
		System.out.println(row);
		String sql = "update hospital set " + column +"='"+value+"'";
		//내가 어디에 있든 맨 앞에 있는게 seq이다.
		sql +=" where seq="+seq;
		//엔터쳤을 때 알아맞춰야 한다. e써야한다.
		//System.out.println(sql);
		PreparedStatement pstmt=null;
		
		try {
			pstmt = con.prepareStatement(sql);
			int result = pstmt.executeUpdate();
			if(result!=0) {
				JOptionPane.showMessageDialog(this, "수정완료");
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
		//insertSql에 insert 문이 몇개있는지 알아보자!
		String[] str = insertSql.toString().split(";");
		System.out.println("insert문 수는"+str.length);
		
		PreparedStatement pstmt =null;
		
		for(int i =0; i<str.length; i++) {
			//System.out.println(str[i]);	
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//try catch 끝나는 곳에 평소대로 insert문 날리면 된다.
			//쿼리문마다 1대1 매칭되서 올라옴
			try {
				pstmt = con.prepareStatement(str[i]);
				int result = pstmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		//기존에 사용했던 StringBuffer 비우기
		insertSql.delete(0, insertSql.length());
		
		if(pstmt!=null) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
		//모든 INSERT가 종료되면 JTable UI갱신
	}

	public static void main(String[] args) {
		new LoadMain();
	}
}
