/*
파일과 관련된 작업을 도와주는 재사용성있는 클래스를 정의한다.
 */

package util.file;
public class FileUtil {
	/* 
	 넘겨받는 경로에서 확장자 구하기 
	 우리는 지금 확장자를 잡아내는경우지만 확장자를 뿌려줄 때도 있다
	 */

	//확장자를 반환할꺼니까 void가 아니고 String이 되어야 한다.
	//이 메서드는 static 안붙어서 instatnce 메서드이다.
	//파일 메모리에 올리는것도 싫으면 static으로 -> file.util. 이렇게 쓸 수 있따.
	public static String getExt(String path) {
		//그 어떤 존재가 와도 우리는 마지막 인덱스만
		int last = path.lastIndexOf(".");
		return path.substring(last+1, path.length());
	}
}
