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
	FileReader reader=null; //reader로 끝났기 때문에 문자기반 스트림이자 입력스트림
	//한줄씩 읽고싶으니까 업그레이드
	BufferedReader buffr =null;
	
	 //윈도우 창이 열리면 이미 db가 접속을 확보해야 한다.
	DBManager manager = DBManager.getInstance();
	Connection con;
	
	public LoadMain() {
		p_north = new JPanel();
		t_path = new JTextField(25);
		bt_open = new JButton("파일열기");
		bt_load = new JButton("로드하기");
		bt_excel = new JButton("엑셀로드");
		bt_del = new JButton("삭제하기");
		
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
		
		//윈도우와 Listener와 연결
		this.addWindowListener(new WindowAdapter() {
			
			public void windowClosing(WindowEvent e) {
				//윈도우창이 닫히면 db 자원해제
				manager.disConnect(con);
				//프로세스 종료
				System.exit(0);
			}
		});
		
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
		int result = chooser.showOpenDialog(this);
		StringBuffer sb_cell = new StringBuffer();
		String data;
		
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
				DataFormatter df = new DataFormatter(); //얘를 이용해 가공

				for(int a=1; a<total; a++){
					HSSFRow row = sheet.getRow(a);
					int columnCount = row.getLastCellNum();
					
					for(int i =0; i<columnCount; i++) {
						HSSFCell cell = row.getCell(i);
						
						//자료형(숫자 or 문자)에 국한되지 않고 모두 String처리해야한다. DataFormatter 사용!
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
	
	// 선택한 레코드 삭제
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

	// 파일 탐색기 띄우기
	public void open() {
		int result = chooser.showOpenDialog(this);
		
		//열기를 누르면 
		//목적 파일에 스트림을 생성하자
		if(result == JFileChooser.APPROVE_OPTION){
			
			File file = chooser.getSelectedFile(); //유저가 선택한 파일
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
