package hjj;

import java.io.BufferedInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;


public class Handle {

	public static int [] cfreq = new int[256];
	public static String [] cHuffCodes = new String[256];   //UNICODE  0x0000--oxffff
	public static LinkedList<HuffNode> clist = new LinkedList<HuffNode>();
	public static HuffNode chuffRoot;
	public static int cfreqLen = 0;
	
	Handle(){
		//��ʼ���ַ�������
		for(int i = 0 ; i < cHuffCodes.length; i++)
			cHuffCodes[i] = "";
	}
	

//����Ƶ��  ����freq[]
public static void calFreq(String fileName, int [] freq) throws Exception{
	
	File file = new File(fileName);
	if(file.isDirectory()) {   //���ļ���	
		File [] files = file.listFiles();
		for(File f: files) 
			calFreq(f.getAbsolutePath(), freq);
	}

	else {   //���ļ�
		BufferedInputStream fis = new BufferedInputStream(new FileInputStream(fileName));	//��ȡ�ļ�		
		int val;           //bufferReader �� read() ��������2bytes�� ���int��4bytes��,  inputstream:��1byte ���int
		while((val= fis.read()) != -1) {   //read()���ص����޷���byte
			freq[val]++;
		}
		fis.close();		
	}	
}
	
	
	
//����HuffTreeʱ����ȡ����ڵ��λ��
public static int getIndex(HuffNode node, LinkedList<HuffNode> list) {
	for(int i =0; i < list.size(); i++) {
		if(node.getData() <= list.get(i).getData())
			return i;
	}
	return list.size();   //�����κ�һ����������뵽�����
}		


//�����������
public static HuffNode buildHuffTree(int [] freq, LinkedList<HuffNode> list ) {
	
	for(int i = 0; i < freq.length; i++) {
		//��freq��Ϊ0�Ĺ�����
		if(freq[i] != 0) {  
			HuffNode node = new HuffNode( freq[i] ,(char) i);
			list.add(getIndex(node, list),node);
			cfreqLen++;   //ͳ��freq��Ϊ0�Ľڵ�����  ��ѹ����ѹҪ��
		}
	}
	
	while(list.size() > 1) {
		
		HuffNode firstNode = list.removeFirst();
		HuffNode secondNode = list.removeFirst();
		HuffNode fatherNode = new HuffNode(firstNode.getData() + secondNode.getData());
		
		fatherNode.setLeft(firstNode);
		fatherNode.setRight(secondNode);
		
		list.add(getIndex(fatherNode, list),fatherNode);
	}
	
//	System.out.println(list.getFirst().getData());
	if(list.isEmpty()) 
		return null;
	else
		return list.getFirst();
} 
	
    
//��ȡ�������������HuffCodes[]	
	public static void getHuffCode(HuffNode root, String code) {
		//�����ߣ������������0		
		if(root.getLeft()!=null){			
			getHuffCode(root.getLeft(),code + "0");		
		}		//�����ߣ������������1		
		if(root.getRight()!=null){			
			getHuffCode(root.getRight(),code + "1");		
		}		
		//�����Ҷ�ӽڵ㣬���ظ�Ҷ�ӽڵ�Ĺ���������		
		if(root.getLeft()==null && root.getRight()==null){
		//	System.out.println(root.getIndex()+"�ı���Ϊ��"+code);
			cHuffCodes[(int)root.getIndex()]=code;		
		}			
	}	
		
///�ݹ齫ÿ���ļ����е��ļ�·������arraylist
	public static ArrayList<String> getAllFilePaths(File filePath, ArrayList<String> filePaths){
		File [] files = filePath.listFiles();
		if(files == null) {
			return filePaths;    
		}
		for(File f: files) {
			if(f.isDirectory()) {
				filePaths.add(f.getPath());
				getAllFilePaths(f, filePaths);
			}
			else
				filePaths.add(f.getPath());
		}
		return filePaths;
	}
	
	
public static void compressSingleFile(String parentPath, File file, BufferedOutputStream bos) throws IOException {
	
	BufferedInputStream input;
	
//���ļ���
	if(file.isDirectory()) {
		//д���ļ�����Ϣ
		//�ļ��б�־
		bos.write(1);
		String relativePath = file.getAbsolutePath().substring(parentPath.length()+1);
		byte [] relPathB = relativePath.getBytes();
		bos.write(relPathB.length);    //д�봢�����·����byte����
		bos.write(relPathB);      //�о��е����⣬��Ϊ�ǽضϺ��λд�룬�����pjӦ��û��
//		bos.flush();	
		
		File[] files = file.listFiles();	
		
		if(files.length == 0)
			return;
		
		for(File innerfile: files) {
			compressSingleFile(parentPath, innerfile, bos);
		}		
	}
//���ļ�
else {
	
	if(parentPath == file.getAbsolutePath()) {   //��������ļ������
		String type = file.getAbsolutePath().substring(parentPath.lastIndexOf(".") + 1);
	//	byte [] typeB = type.getBytes();
		bos.write(type.getBytes().length);    //д�봢�����·����byte����
		bos.write(type.getBytes());      //�о��е����⣬��Ϊ�ǽضϺ��λд�룬�����pjӦ��û��	
		input = new BufferedInputStream(new FileInputStream(parentPath));	//bufferReader��ȡ�ļ�-> 2bytes, ������inputstream	
	}
	
	else {
		//�ļ���ʶ��
		bos.write(2);
		//���·���� �� ����
		String relativePath = file.getAbsolutePath().substring(parentPath.length()+1);
		byte [] relPathB = relativePath.getBytes();
		bos.write(relPathB.length);    //д�봢�����·����byte����
		bos.write(relPathB);      //�о��е����⣬��Ϊ�ǽضϺ��λд�룬�����pjӦ��û��
		input = new BufferedInputStream(new FileInputStream(parentPath + File.separator + relativePath));	//bufferReader��ȡ�ļ�-> 2bytes, ������inputstream
	}
	
	StringBuffer strb = new StringBuffer();
	
	//д���ļ�����
	long fileLen = file.length();    //!Ѫ����  ��Ҫ��for���������file.lengh()��ÿ�ζ�����ã�	
	bos.write(longToByte(fileLen));
	
	if(fileLen == 0)
		return;
	else {
		for( long i = 0; i < fileLen; i++) {    //**���ļ����ȿ��ƶ�ȡ�� ���������-1 �� �ļ��е�-1 ��ͻ
				
				 strb.append(cHuffCodes[input.read() & 0xff]);
				 
				 while(strb.length() >= 8) {  //�߶���д			 
					 int temp = String2Int(strb.toString());    //���ַ�����ǰ��λתΪint
					 bos.write(temp);                            //write()  :��intǿתΪchar���Ͱ�λ�� д���ļ�		 
					 strb = strb.delete(0,8);                  //ɾ����д���8λ
			     }			
		     }	

			//����λ������� 0 ����     //���������Ҫ�油0����
		    
			int temp = String2Int(addZeroRight(strb.toString(), 8));//����Ҫ���Ҽ�0��  �������󣡣�
			bos.write(temp);  //д��(int)temp �ĵ�8λ			
	}
	
	bos.flush();
	input.close();
	}
}
	
	
//���ѹ���ļ�����
public static String compressName(String fileName,String destPath) {	

	File file = new File(fileName);
	String compressName = "";
	if(file.isDirectory())
		compressName = fileName + "hjj.zip";
	else if (file.isFile())
		compressName = fileName.substring(0, fileName.indexOf(".")) + "hjj.zip";
	return compressName;		
	
}

	
	
public void compress(String filePath, String destPath) throws Exception{
	
	String destFile = compressName(filePath,destPath);
		
	ArrayList<String> filePaths = new ArrayList<String>();	
	File file = new File(filePath);	
	BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
  
//�ж��ļ�/�ļ���/�գ�����¼��־
//ͳ��Ƶ�ʴ�������
  
    //�ļ���
	if(file.isDirectory()) {  
		//���ȫ��·��
		filePaths = getAllFilePaths(file,filePaths);
		
		if(filePaths.size() == 0) {    //���ļ���   3
			bos.write(3);   //���ļ��б�־
		//  bos.flush();
			bos.close();
			System.out.println("�ļ���Ϊ�գ�");  
			return;
		}
		else {         //�ǿ��ļ���   1
			//����ͳ������Ƶ��,���빲������
			bos.write(1);   
		//	bos.flush();
			for(String f:filePaths) {
				calFreq(f, cfreq);
			}
		}
	}
	//�ļ�
	else {
		if(!file.exists() || file.length() == 0) {  //���ļ�   4
		    System.out.println("�ļ�Ϊ�գ�");  
		    bos.write(4);   //���ļ���־
		//  bos.flush();
			bos.close();
		    return;
		}  
		else {   //�ǿ��ļ�   2
			bos.write(2);
		//	bos.flush();
			calFreq(filePath, cfreq);
		}
	}

	
	
//�����������huffm����	
	chuffRoot = buildHuffTree(cfreq, clist);
//	System.out.println("Built");
	
	if(chuffRoot != null)
		getHuffCode(chuffRoot, "");
	
//	System.out.println("gotHuffCode");
	
	//д���ַ�Ƶ�ʱ�
	if(cfreqLen == 0) {        
		bos.write(1);  //��־freqSizeΪ0����������ֹ0-1 = 255 �루256-1��������ͻ
        bos.write(0);  //freqSize
	}
	else {
		bos.write(0);   //not freqZero
		bos.write(cfreqLen - 1);    //д��cfreqLen - 1�������256�����������   ��calfreq��ȡ��,
//		StringBuffer freqbuf = new StringBuffer();   //���˲���ֱ��дbyte
		for( int i = 0; i < cfreq.length; i++) {
			if(cfreq[i] != 0) {              //��û�б�Ҫ��ʡ����ռ�
				bos.write((byte)i);                //1 byte
				byte[] t = intToByte(cfreq[i]);
				bos.write(t);   //4 bytes
			}
			else
				continue;
		}
	}

	
//д������Ϣ
		
//�ж����ļ������ļ���
	
    if(file.isDirectory()) {   //ѹ���ļ����ļ���
		File[] files = file.listFiles();   	
		for(File innerfile: files) {   
			compressSingleFile(filePath, innerfile, bos);
		}		
    }	
	else {   //�ǵ����ļ�
		  bos.write(2);   //���ļ��ǵ����ļ��ı�ʶ
	      compressSingleFile(filePath, file, bos);
	}
    bos.close();
    return;
}
	




public void decompress(String fileName,String decodePath){        // throws Exception  (?
	
	long startTime = System.currentTimeMillis();
	
try {
	
	String firstName = fileName.substring(fileName.lastIndexOf(File.separator)+1,fileName.lastIndexOf("."));  //���ȥ����.hjj" ���ļ���
		
	StringBuffer parentPathB = new StringBuffer(decodePath).append("\\").append(firstName).append("new");
	String parentPath = parentPathB.toString();
	
	File firstFile = new File(parentPath);
	
	File srcFile = new File(fileName);      //���ѹ���ļ���С
	long srcSize = srcFile.length();


	BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
	
	int decompressType = bis.read();
	if(decompressType == 1) {//�ļ���
		firstFile.mkdir();
		firstFile.createNewFile();
	}
	else if(decompressType == 2) {//�ļ�
	
	}
	else if(decompressType == 3){   //���ļ���
		firstFile.mkdir();
		firstFile.createNewFile();
		bis.close();
		return;
	}
	else if(decompressType == 4) {//���ļ�
		firstFile.createNewFile();
		bis.close();
		return;
	}
	
	
/*
 * decompressType
 * freqSize
 * freqTable
 * 
 */
	
//�ؽ�Ƶ�ʱ�
	
	LinkedList<HuffNode> reList = new LinkedList<HuffNode>();
	HuffNode reHuffRoot = new HuffNode();

	int freqZero = bis.read();
	int freqSize;
	
	//Ƶ�ʱ�Ϊ��
	if(freqZero == 1) {
		bis.read();
		freqSize = 0;
	}
	//Ƶ�ʱ�Ϊ��
	else {
		freqSize = (bis.read() & 0xff) + 1;    //&0xff: ���������255-->����
		byte [] t = new byte [4] ;
		int [] reFreq = new int[256];   //��ȡƵ�ʱ�
		for(int i = 0; i < freqSize; i++) {
			int index = bis.read() & 0xff;
			bis.read(t, 0, 4);
			reFreq[index] = byteToInt(t);
		}
		
//		String [] reHuffCodes = new String[256];   //UNICODE  0x0000--oxffff	
		reHuffRoot = buildHuffTree(reFreq, reList);
//		System.out.println("reBuilt");
		getHuffCode(reHuffRoot, "");
//		System.out.println("gotHuffCode");
	}
	
	
	//��¼��ѹ���ļ��Ѷ����ݣ��Ա���¶�������
	long readLen = 3 + 5 * freqSize;
	
	
if(decompressType == 1) { //�ļ���
	//ѭ����ȡ�ļ�����ѹ
    do {
    	int isFolder = bis.read();   //1�ļ��У�  2�ļ�
    //�ļ���
    	if(isFolder == 1) {  		
    		
    	    //�ļ�������	
    		int nameLen = bis.read();		
    		//�ļ���
    		byte [] nameB = new byte [nameLen];      //��ȡ�ļ���(���·����
    		bis.read(nameB, 0, nameLen);
    		String name = new String(nameB);
    		
    		//�����ļ���
    		StringBuffer fnameB = new StringBuffer(parentPath).append("\\").append(name);
    	  	File file = new File(fnameB.toString());
            file.mkdir();
            file.createNewFile();
            
            readLen += 5 + nameLen;  //flag(1) + nameLen(4) + nameLen*1
    	}
    	
    //�ļ�
   else if(isFolder == 2){
    		//�ļ�������
            int nameLen = bis.read();
    		
    		//�ļ���
    		byte [] nameB = new byte [nameLen];      //��ȡ�ļ���(���·����
    		bis.read(nameB, 0, nameLen);
    		String name = new String(nameB);
    		
    		//�����ļ�
    		StringBuffer fnameB = new StringBuffer(parentPath).append("\\").append(name);
    		String fname = fnameB.toString();
    		File file = new File(fname);
            file.createNewFile();
            
            //�ļ�����
            byte [] fileLenB = new byte [8];      //��ȡ�ļ���(���·����
    		bis.read(fileLenB, 0, 8);    //long��  8bytes
    		long fileLen = byteToLong(fileLenB);
    		
    		readLen += 2 + nameLen + 8;
    		//��ȡ�ļ����ݣ� ����д��
    		
    		int num = 0;   //��������huff����Ĵ�����
    		HuffNode node = reHuffRoot;
    		StringBuffer strbuf = new StringBuffer();   //�油��0��ƴ���ַ���
    		BufferedOutputStream output =new BufferedOutputStream(new FileOutputStream(fname)); 
    	
    		long count= 0;   //���ļ�byte���ȣ�fileLen��־�Ƿ����
    		int sign = 0;
    		
    	if(fileLen != 0) {	
    		//while �� for ��
    		while(true){    //���ļ����ȿ��ƶ�ȡ�� ���������-1 �� �ļ��е�-1 ��ͻ
    			
    			int value = bis.read();    //input.read() & 0xff;   read byte  ֱ��ת��Ϊint���з�����չ�������һλ��1�������������Ǹ�����
                readLen++;
    			Integer a = new Integer(value);
    			// s ��0��ƴ�� 
    			strbuf.append(addZeroForNum( Integer.toBinaryString(a), 8));     /**   toBinaryString �ĵ�һ���ַ�������0���ᱻɾ��������Ҫ��0   */
    			
    	//		last_num = value;     //���һ������8bits�Ǽ���0��Դ�ļ�����󣬵�����������ѹ���0�ĸ���
    		
    			int flag = 0;       //�ж϶������ڵ�ı�־	
    			
    			while(num < strbuf.length()) {	
					if(strbuf.charAt(num)== '0'){
						if(node.getLeft() == null) {
							flag = 1;
						}
						else
						    node = node.getLeft();
					}
					if(strbuf.charAt(num)== '1'){
						if(node.getRight() == null) {
							flag = 1;
						}
						else
						    node = node.getRight();
					}
					num++;           //�������볤��	
					if(flag == 1) {     //����ѳɹ����һ���ڵ�
					
						output.write((byte)node.getIndex());
					//	decode_strb.append((byte)node.getIndex());  // ��ȡHuffNode.index(int)�ĺ��λ(byte)�ָ�
						                                             // ǿ��ת��Ϊbyte :�з���byte
						strbuf.delete(0, num - 1);    //ɾ���Ѿ������
						flag = 0;                 //��tm��Ȼ��Ϊ������0���˼���Сʱ
						num = 0;    //����0
						node = reHuffRoot;   //���ø��ڵ�		
						count++;
					}	
					if(count >= fileLen) {
						//	if(fileLen == 0)
								sign = 1;
								break;
							}
				}
				if(sign == 1)
					break;			
    		}//end for
    	}//end if fileLen != 0
    	//	output.flush();
    		output.close();
    //		System.out.println(file.length());
        }  
	}while (readLen < srcSize);
}



//�����ļ�
else {  
		bis.read();   //�ļ���־2
		int typeLen = bis.read();
		//��ȡ�ļ���(���·����
		byte [] typeB = new byte [typeLen];      // byte�����ʼ��ȫ����0���������Ĵ�С����������string�Ĵ�С�Ļ�������0��ֱ��ת���ַ�
		bis.read(typeB, 0, typeLen);
		String type = new String(typeB);

		String firstFilePath = parentPath + "."+ type ;     //�Ի���Ϊ��  ������"new"+".pdf,   ������"new.pdf"��new." + "pdf" 
		File singleFile = new File(firstFilePath);     //��ƴ�ַ�����д��ȥ��û���⣿��
		
		//�����ļ�
        singleFile.createNewFile();
        
        //�ļ�����
        byte [] fileLenB = new byte [1024];      //��ȡ�ļ���(���·����
		bis.read(fileLenB, 0, 8);    //long��  8bytes
		long fileLen = byteToLong(fileLenB);
				
		//��ȡ�ļ����ݣ� ����д��
		
		int num = 0;   //��������huff����Ĵ�����
		HuffNode node = reHuffRoot;
		//HuffNode last_node = root;

		StringBuffer strbuf = new StringBuffer();   //�油��0��ƴ���ַ���
		
		BufferedOutputStream output =new BufferedOutputStream(new FileOutputStream(firstFilePath)); 
	
		int count = 0;   //��fileLen��־�Ƿ����
		
		while(true) {    //���ļ����ȿ��ƶ�ȡ�� ���������-1 �� �ļ��е�-1 ��ͻ
			
			int value = bis.read();    //input.read() & 0xff;   read byte  ֱ��ת��Ϊint���з�����չ�������һλ��1�������������Ǹ�����
            /**0xffĬ����int�����棬�������Ƚ� b[0] ǿ��ת��Ϊ int ���ͱ�ʾ��b[0] ����ֵΪ-128������������Ʊ�ʾΪ11111111 11111111 11111111 10000000
                                                                                         0xff�Ķ����Ʊ�ʾΪ 00000000 00000000 00000000 11111111
                                                                                                                                                                                   ���������� & ����󣬵õ� 00000000 00000000 00000000 10000000�����ʾ��ֵΪ128
            */
			Integer a = new Integer(value);
			// s ��0��ƴ�� 
			strbuf.append(addZeroForNum( Integer.toBinaryString(a), 8));     /**   toBinaryString �ĵ�һ���ַ�������0���ᱻɾ��������Ҫ��0   */
			
	//		last_num = value;     //���һ������8bits�Ǽ���0��Դ�ļ�����󣬵�����������ѹ���0�ĸ���
		
			int flag = 0;       //�ж϶������ڵ�ı�־	
			int sign = 0;
			while(num < strbuf.length()) {	
				if(strbuf.charAt(num)== '0'){
					if(node.getLeft() == null) {
						flag = 1;
					}
					else
					    node = node.getLeft();
				}
				if(strbuf.charAt(num)== '1'){
					if(node.getRight() == null) {
						flag = 1;
					}
					else
					    node = node.getRight();
				}
				num++;           //�������볤��	
				if(flag == 1) {     //����ѳɹ����һ���ڵ�
				
					output.write((byte)node.getIndex());
				//	decode_strb.append((byte)node.getIndex());  // ��ȡHuffNode.index(int)�ĺ��λ(byte)�ָ�
					                                             // ǿ��ת��Ϊbyte :�з���byte
					strbuf.delete(0, num - 1);    //ɾ���Ѿ������
					flag = 0;                 //��tm��Ȼ��Ϊ������0���˼���Сʱ
					num = 0;    //����0
					node = reHuffRoot;   //���ø��ڵ�
					count++;
							
				}		
				if(count >= fileLen){
					sign = 1;
					break;
				}
			}
			if(sign == 1)
				break;
		}//end while
		
//		output.flush();
		output.close();
    }  //end else
    bis.close();
}
		
catch (FileNotFoundException e) {
    e.printStackTrace();
   
}
catch (IOException e) {
    e.printStackTrace();
   
}

long endTime = System.currentTimeMillis();
System.out.println("��ѹʱ�䣺" + (endTime - startTime) + "ms");

return;
}


public static String addZeroForNum(String str, int len) {
	StringBuilder sb = new StringBuilder();
		for(int i = str.length() ; i < len; i++) 
             sb.append("0");
		sb.append(str);//��0
	return sb.toString();
}


public static String addZeroRight(String str, int times) {   //�Ҳ�0
	StringBuilder sb = new StringBuilder(str);
		for(int i = str.length() ; i < times; i++) 
             sb.append("0");
	return sb.toString();
}


public static int String2Int( String b) {
    return (((b.charAt(0) - 48) << 7)
           + ((b.charAt(1) - 48) << 6) + ((b.charAt(2) - 48) << 5)
           + ((b.charAt(3) - 48) << 4) + ((b.charAt(4) - 48) << 3)
           + ((b.charAt(5) - 48) << 2) + ((b.charAt(6) - 48) << 1)
           + (b.charAt(7) - 48));
	/*
	int t1=(s.charAt(0)-48)*128;
   int t2=(s.charAt(1)-48)*64;
   int t3=(s.charAt(2)-48)*32;
   int t4=(s.charAt(3)-48)*16;
   int t5=(s.charAt(4)-48)*8;
   int t6=(s.charAt(5)-48)*4;
   int t7=(s.charAt(6)-48)*2;
   int t8=(s.charAt(7)-48)*1;
   return t1+t2+t3+t4+t5+t6+t7+t8;    (1.txt:  8033ms
   */
}
    
    public static byte[] longToByte(long num) {
    	long temp = num;
    	byte [] b = new byte[8];
    	for(int i = 0;i < b.length; i++) {
    		b[i] = new Long(temp & 0xff).byteValue();
    		temp = temp >> 8;
    	}
    	return b;
    }
    
    public static long byteToLong(byte[] b) {  
        long s = 0;  
        long s0 = b[0] & 0xff;// ���λ  
        long s1 = b[1] & 0xff;  
        long s2 = b[2] & 0xff;  
        long s3 = b[3] & 0xff;  
        long s4 = b[4] & 0xff;// ���λ  
        long s5 = b[5] & 0xff;  
        long s6 = b[6] & 0xff;  
        long s7 = b[7] & 0xff;  
  
        // s0����  
        s1 <<= 8;  
        s2 <<= 16;  
        s3 <<= 24;  
        s4 <<= 8 * 4;  
        s5 <<= 8 * 5;  
        s6 <<= 8 * 6;  
        s7 <<= 8 * 7;  
        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;  
        return s;  
    }  
    
    /** 
     * int���ֽ������ת��. 
     */  
    public static byte[] intToByte(int number) {  
        int temp = number;  
        byte[] b = new byte[4];  
        for (int i = 0; i < b.length; i++) {  
            b[i] = new Integer(temp & 0xff).byteValue();// �����λ���������λ  
            temp = temp >> 8;// ������8λ  
        }  
        return b;  
    }  
  
    /** 
     * �ֽ����鵽int��ת��. 
     */  
    public static int byteToInt(byte[] b) {  
        int s = 0;  
        int s0 = b[0] & 0xff;// ���λ  
        int s1 = b[1] & 0xff;  
        int s2 = b[2] & 0xff;  
        int s3 = b[3] & 0xff;  
        s3 <<= 24;  
        s2 <<= 16;  
        s1 <<= 8;  
        s = s0 | s1 | s2 | s3;  
        return s;  
    }  
	

}


/* FileInputStream is = new FileInputStream(fileName);
    byte[] b = new byte[1024];
    int len = 0;
    while((len=is.read(b))!=-1){
        System.out.println(new String(b,0,len));
    }
    is.close();
    
*/
/*//��ӡ�ļ��Ķ�������
	 FileInputStream is = new FileInputStream(fileName);
	 PrintWriter output = new PrintWriter("C:\\Users\\HULEI\\Desktop\\decode_binarytest.txt");
	 int test;
		
		while((test=is.read()) != -1) {
		//	Integer a = new Integer(test);
			//System.out.print(Integer.toBinaryString(test));

			output.println(Integer.toBinaryString(test));	
		}
		output.close();
		is.close();	
//  */	
	
//	byte[] buffer = File.ReadAllBytes(fileName);    //�ļ��ܴ�ʱ���ܻỺ�����
	
/**          �ɰ浥���ļ�     */
/*
	File file = new File(fileName);
	
	long fileLen = file.length();   //��ȡ�ļ����ȣ�n bytes
	System.out.println("fileLength: "+fileLen);
	
	long startTime = System.currentTimeMillis();
	
	
//	System.out.println("Start-read");
	long startRead = System.currentTimeMillis();
	
	try {
// �ɰ��ѹ��ȡ

/*		
		while((value = input.read()) != -1) {
			Integer a = new Integer(value);
			// System.out.println("index: "+value);
			last_str = str;   //������һ���ַ���
			//��Ϊcompress�����ȷ�Ϊ8bitsһ�ݣ���ת��Ϊint����������Ҫ��intת��01������8λ
			String s = Integer.toBinaryString(value);
			
			for(int i = s.length() ; i < 8; i++) {
				strbuf.append("0").append(s);   //��0   ??buffer
				str = strbuf.toString();
				}			
		}		
	*/	
/*		
		int num = 0;   //��������huff����Ĵ�����
		HuffNode node = chuffRoot;
		//HuffNode last_node = root;
		String decode_str ="";

		StringBuilder decode_strb = new StringBuilder();  //�ַ���ƴ�ӣ����߳�stringBuilder > stringBuffer(�̰߳�ȫ��>string	
		StringBuffer strbuf = new StringBuffer();   //�油��0��ƴ���ַ���
		
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(fileName));	//bufferReader��ȡ�ļ�-> 2bytes, ������inputstream
		BufferedOutputStream output = new BufferedOutputStream( new FileOutputStream(destFile));
		
	//	String delete_buf;
		
		for( int i = 0; i < fileLen - 2; i++) {    //���ļ����ȿ��ƶ�ȡ�� ���������-1 �� �ļ��е�-1 ��ͻ
			
			int value = input.read();    //input.read() & 0xff;   read byte  ֱ��ת��Ϊint���з�����չ�������һλ��1�������������Ǹ�����
            //0xffĬ����int�����棬�������Ƚ� b[0] ǿ��ת��Ϊ int ���ͱ�ʾ��b[0] ����ֵΪ-128������������Ʊ�ʾΪ11111111 11111111 11111111 10000000
            //                                                                            0xff�Ķ����Ʊ�ʾΪ 00000000 00000000 00000000 11111111
                                                                                                                                                                                 ���������� & ����󣬵õ� 00000000 00000000 00000000 10000000�����ʾ��ֵΪ128
			Integer a = new Integer(value);
			// s ��0��ƴ�� 
			strbuf.append(addZeroForNum( Integer.toBinaryString(a), 8));     //   toBinaryString �ĵ�һ���ַ�������0���ᱻɾ��������Ҫ��0   
			
	//		last_num = value;     //���һ������8bits�Ǽ���0��Դ�ļ�����󣬵�����������ѹ���0�ĸ���
		
			int flag = 0;       //�ж϶������ڵ�ı�־	
			while(num < strbuf.length()) {	
				if(strbuf.charAt(num)== '0'){
					if(node.getLeft() == null) {
						flag = 1;
					}
					else
					    node = node.getLeft();
				}
				if(strbuf.charAt(num)== '1'){
					if(node.getRight() == null) {
						flag = 1;
					}
					else
					    node = node.getRight();
				}
				num++;           //�������볤��	
				if(flag == 1) {     //����ѳɹ����һ���ڵ�
				
					output.write((byte)node.getIndex());
				//	decode_strb.append((byte)node.getIndex());  // ��ȡHuffNode.index(int)�ĺ��λ(byte)�ָ�
					                                             // ǿ��ת��Ϊbyte :�з���byte
					strbuf.delete(0, num - 1);    //ɾ���Ѿ������
					flag = 0;                 //��tm��Ȼ��Ϊ������0���˼���Сʱ
					num = 0;    //����0
					node = chuffRoot;   //���ø��ڵ�
				}		
				
			}
	//		delete_buf = strbuf.toString();
		}
		
		Integer last_s = new Integer(input.read());
		int last_n = input.read();
		strbuf.append(addZeroForNum(Integer.toBinaryString(last_s),8).substring(last_n));   //�Ѽ�ȥ0 �����һ���ַ����ӵ�����
		
		int flag = 0;       //�ж϶������ڵ�ı�־	
		num = 0;
		node = chuffRoot;
		while(num < strbuf.length()) {		
			if((int)(strbuf.charAt(num) - 48)==0){
				if(node.getLeft() == null) {
					flag = 1;
				}
				else
				    node = node.getLeft();
			}
			if((int)(strbuf.charAt(num) - 48)==1){
				if(node.getRight() == null) {
					flag = 1;
				}
				else
					node = node.getRight();
			}
			num++;           //�������볤��	
			if(flag == 1) {     //����ѳɹ����һ���ڵ�
				output.write((byte)node.getIndex());
				//decode_strb.append((byte)node.getIndex() + "");  // ��ȡtreenode(int)�ĺ��λ(byte)�ָ�
				// ǿ��ת��Ϊbyte :�з���byte��
				strbuf.delete(0, num-1);    //ɾ���Ѿ������
			//	delete_buf = strbuf.toString();
				num = 0;    //����0
				flag = 0;
				node = chuffRoot;
			}	
		}
		
		
		input.close();
		output.flush();
		output.close();
*/
		

/*		
	int value = input.read();

	while(value != -1) {
		Integer a = new Integer(value);
		
		// System.out.println("index: "+value);	
		//��Ϊcompress�����ȷ�Ϊ8bitsһ�ݣ���ת��Ϊint����������Ҫ��intת��01������8λ		
		String s = Integer.toBinaryString(a);
		last_str = s;   //������һ���ַ���(δ��0
		strbuf.append(addZeroForNum(s, 8));   // s ��0��ƴ��
		last_num = value;     //���һ������8bits�Ǽ���0��Դ�ļ�����󣬵�����������ѹ���0�ĸ���
		value = input.read();
	//	System.out.println(str);
	    }
	input.close();
	//�� 
	
	System.out.println("Decoding: End-read.");	
    }
	
    catch(IOException e){
		
	}	
	
	long endRead = System.currentTimeMillis();
	System.out.println("Decoding...  Read Time :" + (endRead-startRead));
	
	
	System.out.println("last_num:" + last_num);
	System.out.println("last_str:" + last_str);
	str = strbuf.toString();
	str = str.substring(0,str.length() - last_num);   //ȥ�������0(last_num ��
	System.out.println("str.length:" + str.length());
	
//Huffm����	
	
//	int a =(int)(str.charAt(num) - 48);
/*	HuffNode node = null;
	HuffNode last_node = root;
	String decode_str ="";

	StringBuilder decode_strb = new StringBuilder();  //�ַ���ƴ�ӣ����߳�stringBuilder > stringBuffer(�̰߳�ȫ��>string	
/*	
	long startHuff = System.currentTimeMillis();
	
	
	//ԭ����
/*
	while(str.length() > 0) {
		while(true) {
			if((int)(str.charAt(num) - 48)==0){
				node = last_node.getLeft();
				if(node == null)
					break;
			}
			if((int)(str.charAt(num) - 48)==1){
				node = last_node.getRight();
				if(node == null)
					break;
			}
			last_node = node;
			num++;           //�������볤��
			if(num > str.length() - 1) 
				break;			
		}
		decode_strb.append((char)last_node.getIndex()).append("");   //ΪʲôҪ�ӡ�����
	    str = str.substring(num);   //���Ѿ�����Ĵ�ȡ��
	    num = 0;
	    last_node = root;
	}


	while(str.length() > 0) {
		
		while(num < str.length()) {
			if((int)(str.charAt(num) - 48)==0){
				node = last_node.getLeft();
				if(node == null)
					break;
			}
			if((int)(str.charAt(num) - 48)==1){
				node = last_node.getRight();
				if(node == null)
					break;
			}
			last_node = node;
			num++;           //�������볤��
			if(num > str.length() - 1) 
				break;			
		}
		decode_strb.append((char)last_node.getIndex());   //ΪʲôҪ�ӡ�����
	    str = str.substring(num);   //���Ѿ�����Ĵ�ȡ��
	    num = 0;
	    last_node = root;
	}
*/
		
	
	/*���ַ�����ӡ���ļ�
	PrintWriter decode_out = new PrintWriter(destFile);
 //	decode_out.println(decode_strb.toString());	
	decode_out.println(decode_strb.toString());	
	decode_out.close();
*/	
	
  /*  FileWriter fWriter = new FileWriter(testFile, true);
    BufferedWriter bufferedWriter = new BufferedWriter(fWriter);
	bufferedWriter.write(decode_str);
  */  


/*	
	//��ӡ�����code
	FileInputStream is2 = new FileInputStream(fileName);
	 PrintWriter output2 = new PrintWriter("C:\\Users\\HULEI\\Desktop\\decode_binarytest.txt");
	 int test2;
		
		while((test2=is2.read()) != -1) {
		//	Integer a = new Integer(test);
			//System.out.print(Integer.toBinaryString(test));

			output2.println(Integer.toBinaryString(test2));	
		}
		output2.close();
		is2.close();	
	*/
	


