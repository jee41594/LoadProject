/*
���ϰ� ���õ� �۾��� �����ִ� ���뼺�ִ� Ŭ������ �����Ѵ�.
 */

package util.file;
public class FileUtil {
	/* 
	 �Ѱܹ޴� ��ο��� Ȯ���� ���ϱ� 
	 �츮�� ���� Ȯ���ڸ� ��Ƴ��°������ Ȯ���ڸ� �ѷ��� ���� �ִ�
	 */

	//Ȯ���ڸ� ��ȯ�Ҳ��ϱ� void�� �ƴϰ� String�� �Ǿ�� �Ѵ�.
	//�� �޼���� static �Ⱥپ instatnce �޼����̴�.
	//���� �޸𸮿� �ø��°͵� ������ static���� -> file.util. �̷��� �� �� �ֵ�.
	public static String getExt(String path) {
		//�� � ���簡 �͵� �츮�� ������ �ε�����
		int last = path.lastIndexOf(".");
		return path.substring(last+1, path.length());
	}
}
