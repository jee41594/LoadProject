/*
JTable�� ���÷� ������ ���� ��Ʈ�ѷ�!
 */
package oracle;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class MyModel extends AbstractTableModel{
	
	Vector columnName; //�÷��� ������ ���� ����
	Vector<Vector> list; //���ڵ带 ���� ������ ����
	
	//������ ������ rs �� LoadMain�� �����Ƿ� �ű⼭ �����ؼ� �Ѱ�����.
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
	
	//���̺� �� cell ������ �� �ִ��� �ƴ��� �̰ɷΤ�!
	//row, col�� ��ġ�� ���� ���� �����ϰ� �Ѵ�.
	public boolean isCellEditable(int row, int col) {
		return true;
		//�׻� ������ ����!!
	}

	//������ ���� �ݿ��ϴ� �޼��� �������̵� 
	//row, col �ǵ����͸� value Object�� �ݿ��Ѵٴ� �ǹ�!
	//2���� Vector���� �������� ���̹Ƿ� 2���� �迭���� �����ؾ� �Ѵ�.
	public void setValueAt(Object value, int row, int col) {
		//��, ȣ���� �����Ѵ�.
		Vector vec = list.get(row); //���� ��ü�� �ǹ� -> ������������ �뱸���������� �𸥴�.
		vec.set(col, value); //ȣ������ value�� �ְڴ�.
		this.fireTableDataChanged();
		//this.fireTableCellUpdated(row, col);
	}
	
	public Object getValueAt(int row, int col) {
		//������ Vector�� �����!
		Vector vec = list.get(row);
		return vec.elementAt(col);
	}
}
