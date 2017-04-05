/*
JTable이 수시로 정보를 얻어가는 컨트롤러!
 */
package oracle;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class MyModel extends AbstractTableModel{
	
	Vector columnName; //컬럼의 제목을 담을 백터
	Vector<Vector> list; //레코드를 담을 이차원 백터
	
	//벡터의 구성은 rs 가 LoadMain에 있으므로 거기서 가공해서 넘겨주자.
	public MyModel(Vector list, Vector columnName) {
		this.list = list;
		this.columnName=columnName;		
	}

	public int getColumnCount() {
		return columnName.size();
	}
	

	public String getColumnName(int col) {
		return (String)columnName.elementAt(col);
	}

	public int getRowCount() {
		return list.size();
	}
	
	//테이블 각 cell 편집할 수 있는지 아닌지 이걸로ㅗ!
	//row, col에 위치한 셀을 편집 가능하게 한다.
	public boolean isCellEditable(int row, int col) {
		return true;
		//항상 편집이 가능!!
	}

	//각셀의 값을 반영하는 메서드 오버라이드 
	//row, col 의데이터를 value Object로 반영한다는 의미!
	//2차원 Vector에서 가져오는 것이므로 2차원 배열에서 수정해야 한다.
	public void setValueAt(Object value, int row, int col) {
		//층, 호수를 변경한다.
		Vector vec = list.get(row); //한줄 전체를 의미 -> 동물병원인지 대구광역시인지 모른다.
		vec.set(col, value); //호수에는 value를 넣겠다.
		this.fireTableDataChanged();
		//this.fireTableCellUpdated(row, col);
	}
	
	public Object getValueAt(int row, int col) {
		//이차원 Vector를 썼었음!
		Vector vec = list.get(row);
		return vec.elementAt(col);
	}
}
