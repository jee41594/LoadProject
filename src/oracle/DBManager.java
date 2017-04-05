/*
Single ton 패턴으로 만들자

 */
package oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
	static private DBManager instance;
	
	private String driver="oracle.jdbc.driver.OracleDriver";
	private String url="jdbc:oracle:thin:@localhost:1521:XE";
	private String user="batman";
	private String password="1234";
	
	Connection con; //접속 후 그 정보 담는 객체

	//new 막기 위함
	private DBManager() {
		/*
		 1. 드라이버 로드
		 2. 접속
		 3. 쿼리실행
		 4. 반납
		 */
		
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//왜 static으로?
	//instance를 new하지않고도 호출하게 하려고!
	static public DBManager getInstance() {
		
		if(instance==null) {
			instance = new DBManager();
		}
		return instance;
	}
	
	//접속객체 반환
	public Connection getConncection() {
		return con;
	}
	
	public void disConnect(Connection con){
		if(con!=null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	//접속해제
}
