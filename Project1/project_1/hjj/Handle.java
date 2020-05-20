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
		//初始化字符串数组
		for(int i = 0 ; i < cHuffCodes.length; i++)
			cHuffCodes[i] = "";
	}
	

//计算频率  存入freq[]
public static void calFreq(String fileName, int [] freq) throws Exception{
	
	File file = new File(fileName);
	if(file.isDirectory()) {   //是文件夹	
		File [] files = file.listFiles();
		for(File f: files) 
			calFreq(f.getAbsolutePath(), freq);
	}

	else {   //是文件
		BufferedInputStream fis = new BufferedInputStream(new FileInputStream(fileName));	//读取文件		
		int val;           //bufferReader 的 read() 方法：读2bytes， 存成int（4bytes）,  inputstream:读1byte 存成int
		while((val= fis.read()) != -1) {   //read()返回的是无符号byte
			freq[val]++;
		}
		fis.close();		
	}	
}
	
	
	
//构造HuffTree时，获取插入节点的位置
public static int getIndex(HuffNode node, LinkedList<HuffNode> list) {
	for(int i =0; i < list.size(); i++) {
		if(node.getData() <= list.get(i).getData())
			return i;
	}
	return list.size();   //若比任何一个数大，则插入到最后面
}		


//构造哈夫曼树
public static HuffNode buildHuffTree(int [] freq, LinkedList<HuffNode> list ) {
	
	for(int i = 0; i < freq.length; i++) {
		//将freq不为0的构造树
		if(freq[i] != 0) {  
			HuffNode node = new HuffNode( freq[i] ,(char) i);
			list.add(getIndex(node, list),node);
			cfreqLen++;   //统计freq不为0的节点数量  （压缩解压要用
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
	
    
//获取哈夫曼编码存入HuffCodes[]	
	public static void getHuffCode(HuffNode root, String code) {
		//往左走，哈夫曼编码加0		
		if(root.getLeft()!=null){			
			getHuffCode(root.getLeft(),code + "0");		
		}		//往右走，哈夫曼编码加1		
		if(root.getRight()!=null){			
			getHuffCode(root.getRight(),code + "1");		
		}		
		//如果是叶子节点，返回该叶子节点的哈夫曼编码		
		if(root.getLeft()==null && root.getRight()==null){
		//	System.out.println(root.getIndex()+"的编码为："+code);
			cHuffCodes[(int)root.getIndex()]=code;		
		}			
	}	
		
///递归将每层文件夹中的文件路径存入arraylist
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
	
//是文件夹
	if(file.isDirectory()) {
		//写入文件夹信息
		//文件夹标志
		bos.write(1);
		String relativePath = file.getAbsolutePath().substring(parentPath.length()+1);
		byte [] relPathB = relativePath.getBytes();
		bos.write(relPathB.length);    //写入储存相对路径的byte长度
		bos.write(relPathB);      //感觉有点问题，因为是截断后八位写入，但这个pj应该没事
//		bos.flush();	
		
		File[] files = file.listFiles();	
		
		if(files.length == 0)
			return;
		
		for(File innerfile: files) {
			compressSingleFile(parentPath, innerfile, bos);
		}		
	}
//是文件
else {
	
	if(parentPath == file.getAbsolutePath()) {   //最外层是文件的情况
		String type = file.getAbsolutePath().substring(parentPath.lastIndexOf(".") + 1);
	//	byte [] typeB = type.getBytes();
		bos.write(type.getBytes().length);    //写入储存相对路径的byte长度
		bos.write(type.getBytes());      //感觉有点问题，因为是截断后八位写入，但这个pj应该没事	
		input = new BufferedInputStream(new FileInputStream(parentPath));	//bufferReader读取文件-> 2bytes, 还是用inputstream	
	}
	
	else {
		//文件标识符
		bos.write(2);
		//相对路径名 及 长度
		String relativePath = file.getAbsolutePath().substring(parentPath.length()+1);
		byte [] relPathB = relativePath.getBytes();
		bos.write(relPathB.length);    //写入储存相对路径的byte长度
		bos.write(relPathB);      //感觉有点问题，因为是截断后八位写入，但这个pj应该没事
		input = new BufferedInputStream(new FileInputStream(parentPath + File.separator + relativePath));	//bufferReader读取文件-> 2bytes, 还是用inputstream
	}
	
	StringBuffer strb = new StringBuffer();
	
	//写入文件长度
	long fileLen = file.length();    //!血案！  不要在for的条件里放file.lengh()！每次都会调用！	
	bos.write(longToByte(fileLen));
	
	if(fileLen == 0)
		return;
	else {
		for( long i = 0; i < fileLen; i++) {    //**用文件长度控制读取： 以免结束符-1 和 文件中的-1 冲突
				
				 strb.append(cHuffCodes[input.read() & 0xff]);
				 
				 while(strb.length() >= 8) {  //边读边写			 
					 int temp = String2Int(strb.toString());    //把字符串的前八位转为int
					 bos.write(temp);                            //write()  :把int强转为char（低八位） 写入文件		 
					 strb = strb.delete(0,8);                  //删掉已写入的8位
			     }			
		     }	

			//最后八位不足的用 0 补齐     //这里好像不需要存补0个数
		    
			int temp = String2Int(addZeroRight(strb.toString(), 8));//补齐要向右加0！  不是向左！！
			bos.write(temp);  //写入(int)temp 的低8位			
	}
	
	bos.flush();
	input.close();
	}
}
	
	
//获得压缩文件名字
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
  
//判断文件/文件夹/空，并记录标志
//统计频率存入数组
  
    //文件夹
	if(file.isDirectory()) {  
		//获得全部路径
		filePaths = getAllFilePaths(file,filePaths);
		
		if(filePaths.size() == 0) {    //空文件夹   3
			bos.write(3);   //空文件夹标志
		//  bos.flush();
			bos.close();
			System.out.println("文件夹为空！");  
			return;
		}
		else {         //非空文件夹   1
			//遍历统计所有频率,存入共享数组
			bos.write(1);   
		//	bos.flush();
			for(String f:filePaths) {
				calFreq(f, cfreq);
			}
		}
	}
	//文件
	else {
		if(!file.exists() || file.length() == 0) {  //空文件   4
		    System.out.println("文件为空！");  
		    bos.write(4);   //空文件标志
		//  bos.flush();
			bos.close();
		    return;
		}  
		else {   //非空文件   2
			bos.write(2);
		//	bos.flush();
			calFreq(filePath, cfreq);
		}
	}

	
	
//构造树，获得huffm编码	
	chuffRoot = buildHuffTree(cfreq, clist);
//	System.out.println("Built");
	
	if(chuffRoot != null)
		getHuffCode(chuffRoot, "");
	
//	System.out.println("gotHuffCode");
	
	//写入字符频率表
	if(cfreqLen == 0) {        
		bos.write(1);  //标志freqSize为0————防止0-1 = 255 与（256-1）发生冲突
        bos.write(0);  //freqSize
	}
	else {
		bos.write(0);   //not freqZero
		bos.write(cfreqLen - 1);    //写入cfreqLen - 1：如果有256个，会溢出；   从calfreq里取得,
//		StringBuffer freqbuf = new StringBuffer();   //用了不能直接写byte
		for( int i = 0; i < cfreq.length; i++) {
			if(cfreq[i] != 0) {              //有没有必要节省这个空间
				bos.write((byte)i);                //1 byte
				byte[] t = intToByte(cfreq[i]);
				bos.write(t);   //4 bytes
			}
			else
				continue;
		}
	}

	
//写入总信息
		
//判断是文件还是文件夹
	
    if(file.isDirectory()) {   //压缩文件是文件夹
		File[] files = file.listFiles();   	
		for(File innerfile: files) {   
			compressSingleFile(filePath, innerfile, bos);
		}		
    }	
	else {   //是单个文件
		  bos.write(2);   //总文件是单个文件的标识
	      compressSingleFile(filePath, file, bos);
	}
    bos.close();
    return;
}
	




public void decompress(String fileName,String decodePath){        // throws Exception  (?
	
	long startTime = System.currentTimeMillis();
	
try {
	
	String firstName = fileName.substring(fileName.lastIndexOf(File.separator)+1,fileName.lastIndexOf("."));  //获得去掉“.hjj" 的文件名
		
	StringBuffer parentPathB = new StringBuffer(decodePath).append("\\").append(firstName).append("new");
	String parentPath = parentPathB.toString();
	
	File firstFile = new File(parentPath);
	
	File srcFile = new File(fileName);      //获得压缩文件大小
	long srcSize = srcFile.length();


	BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
	
	int decompressType = bis.read();
	if(decompressType == 1) {//文件夹
		firstFile.mkdir();
		firstFile.createNewFile();
	}
	else if(decompressType == 2) {//文件
	
	}
	else if(decompressType == 3){   //空文件夹
		firstFile.mkdir();
		firstFile.createNewFile();
		bis.close();
		return;
	}
	else if(decompressType == 4) {//空文件
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
	
//重建频率表
	
	LinkedList<HuffNode> reList = new LinkedList<HuffNode>();
	HuffNode reHuffRoot = new HuffNode();

	int freqZero = bis.read();
	int freqSize;
	
	//频率表为空
	if(freqZero == 1) {
		bis.read();
		freqSize = 0;
	}
	//频率表不为空
	else {
		freqSize = (bis.read() & 0xff) + 1;    //&0xff: 如果不做，255-->负数
		byte [] t = new byte [4] ;
		int [] reFreq = new int[256];   //读取频率表
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
	
	
	//记录解压该文件已读数据，以便更新读的坐标
	long readLen = 3 + 5 * freqSize;
	
	
if(decompressType == 1) { //文件夹
	//循环读取文件并解压
    do {
    	int isFolder = bis.read();   //1文件夹；  2文件
    //文件夹
    	if(isFolder == 1) {  		
    		
    	    //文件名长度	
    		int nameLen = bis.read();		
    		//文件名
    		byte [] nameB = new byte [nameLen];      //读取文件名(相对路径）
    		bis.read(nameB, 0, nameLen);
    		String name = new String(nameB);
    		
    		//创建文件夹
    		StringBuffer fnameB = new StringBuffer(parentPath).append("\\").append(name);
    	  	File file = new File(fnameB.toString());
            file.mkdir();
            file.createNewFile();
            
            readLen += 5 + nameLen;  //flag(1) + nameLen(4) + nameLen*1
    	}
    	
    //文件
   else if(isFolder == 2){
    		//文件名长度
            int nameLen = bis.read();
    		
    		//文件名
    		byte [] nameB = new byte [nameLen];      //读取文件名(相对路径）
    		bis.read(nameB, 0, nameLen);
    		String name = new String(nameB);
    		
    		//创建文件
    		StringBuffer fnameB = new StringBuffer(parentPath).append("\\").append(name);
    		String fname = fnameB.toString();
    		File file = new File(fname);
            file.createNewFile();
            
            //文件长度
            byte [] fileLenB = new byte [8];      //读取文件名(相对路径）
    		bis.read(fileLenB, 0, 8);    //long型  8bytes
    		long fileLen = byteToLong(fileLenB);
    		
    		readLen += 2 + nameLen + 8;
    		//读取文件内容， 解码写入
    		
    		int num = 0;   //保存正在huff解码的串长度
    		HuffNode node = reHuffRoot;
    		StringBuffer strbuf = new StringBuffer();   //存补了0的拼接字符串
    		BufferedOutputStream output =new BufferedOutputStream(new FileOutputStream(fname)); 
    	
    		long count= 0;   //用文件byte长度：fileLen标志是否读完
    		int sign = 0;
    		
    	if(fileLen != 0) {	
    		//while 比 for 快
    		while(true){    //用文件长度控制读取： 以免结束符-1 和 文件中的-1 冲突
    			
    			int value = bis.read();    //input.read() & 0xff;   read byte  直接转换为int是有符号扩展，如果第一位是1，则最后读出来是个负数
                readLen++;
    			Integer a = new Integer(value);
    			// s 补0后拼接 
    			strbuf.append(addZeroForNum( Integer.toBinaryString(a), 8));     /**   toBinaryString 的第一个字符不能是0，会被删掉，所以要补0   */
    			
    	//		last_num = value;     //最后一个整的8bits是加了0的源文件的最后，单独出来的是压入的0的个数
    		
    			int flag = 0;       //判断读到根节点的标志	
    			
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
					num++;           //数出编码长度	
					if(flag == 1) {     //如果已成功获得一个节点
					
						output.write((byte)node.getIndex());
					//	decode_strb.append((byte)node.getIndex());  // 截取HuffNode.index(int)的后八位(byte)恢复
						                                             // 强制转换为byte :有符号byte
						strbuf.delete(0, num - 1);    //删除已经解码的
						flag = 0;                 //我tm竟然因为忘记置0调了几个小时
						num = 0;    //重置0
						node = reHuffRoot;   //重置根节点		
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



//单个文件
else {  
		bis.read();   //文件标志2
		int typeLen = bis.read();
		//读取文件名(相对路径）
		byte [] typeB = new byte [typeLen];      // byte数组初始化全部置0，如果定义的大小大于里面存的string的大小的话，遇到0就直接转空字符
		bis.read(typeB, 0, typeLen);
		String type = new String(typeB);

		String firstFilePath = parentPath + "."+ type ;     //迷惑行为：  不能用"new"+".pdf,   可以用"new.pdf"或“new." + "pdf" 
		File singleFile = new File(firstFilePath);     //先拼字符串再写进去就没问题？？
		
		//创建文件
        singleFile.createNewFile();
        
        //文件长度
        byte [] fileLenB = new byte [1024];      //读取文件名(相对路径）
		bis.read(fileLenB, 0, 8);    //long型  8bytes
		long fileLen = byteToLong(fileLenB);
				
		//读取文件内容， 解码写入
		
		int num = 0;   //保存正在huff解码的串长度
		HuffNode node = reHuffRoot;
		//HuffNode last_node = root;

		StringBuffer strbuf = new StringBuffer();   //存补了0的拼接字符串
		
		BufferedOutputStream output =new BufferedOutputStream(new FileOutputStream(firstFilePath)); 
	
		int count = 0;   //用fileLen标志是否读完
		
		while(true) {    //用文件长度控制读取： 以免结束符-1 和 文件中的-1 冲突
			
			int value = bis.read();    //input.read() & 0xff;   read byte  直接转换为int是有符号扩展，如果第一位是1，则最后读出来是个负数
            /**0xff默认用int来保存，所以首先将 b[0] 强制转化为 int 类型表示，b[0] 的数值为-128，所以其二进制表示为11111111 11111111 11111111 10000000
                                                                                         0xff的二进制表示为 00000000 00000000 00000000 11111111
                                                                                                                                                                                   这两个数做 & 运算后，得到 00000000 00000000 00000000 10000000，其表示的值为128
            */
			Integer a = new Integer(value);
			// s 补0后拼接 
			strbuf.append(addZeroForNum( Integer.toBinaryString(a), 8));     /**   toBinaryString 的第一个字符不能是0，会被删掉，所以要补0   */
			
	//		last_num = value;     //最后一个整的8bits是加了0的源文件的最后，单独出来的是压入的0的个数
		
			int flag = 0;       //判断读到根节点的标志	
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
				num++;           //数出编码长度	
				if(flag == 1) {     //如果已成功获得一个节点
				
					output.write((byte)node.getIndex());
				//	decode_strb.append((byte)node.getIndex());  // 截取HuffNode.index(int)的后八位(byte)恢复
					                                             // 强制转换为byte :有符号byte
					strbuf.delete(0, num - 1);    //删除已经解码的
					flag = 0;                 //我tm竟然因为忘记置0调了几个小时
					num = 0;    //重置0
					node = reHuffRoot;   //重置根节点
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
System.out.println("解压时间：" + (endTime - startTime) + "ms");

return;
}


public static String addZeroForNum(String str, int len) {
	StringBuilder sb = new StringBuilder();
		for(int i = str.length() ; i < len; i++) 
             sb.append("0");
		sb.append(str);//左补0
	return sb.toString();
}


public static String addZeroRight(String str, int times) {   //右补0
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
        long s0 = b[0] & 0xff;// 最低位  
        long s1 = b[1] & 0xff;  
        long s2 = b[2] & 0xff;  
        long s3 = b[3] & 0xff;  
        long s4 = b[4] & 0xff;// 最低位  
        long s5 = b[5] & 0xff;  
        long s6 = b[6] & 0xff;  
        long s7 = b[7] & 0xff;  
  
        // s0不变  
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
     * int到字节数组的转换. 
     */  
    public static byte[] intToByte(int number) {  
        int temp = number;  
        byte[] b = new byte[4];  
        for (int i = 0; i < b.length; i++) {  
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位  
            temp = temp >> 8;// 向右移8位  
        }  
        return b;  
    }  
  
    /** 
     * 字节数组到int的转换. 
     */  
    public static int byteToInt(byte[] b) {  
        int s = 0;  
        int s0 = b[0] & 0xff;// 最低位  
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
/*//打印文件的二进制码
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
	
//	byte[] buffer = File.ReadAllBytes(fileName);    //文件很大时可能会缓存溢出
	
/**          旧版单个文件     */
/*
	File file = new File(fileName);
	
	long fileLen = file.length();   //获取文件长度（n bytes
	System.out.println("fileLength: "+fileLen);
	
	long startTime = System.currentTimeMillis();
	
	
//	System.out.println("Start-read");
	long startRead = System.currentTimeMillis();
	
	try {
// 旧版解压读取

/*		
		while((value = input.read()) != -1) {
			Integer a = new Integer(value);
			// System.out.println("index: "+value);
			last_str = str;   //保存上一个字符串
			//因为compress中是先分为8bits一份，再转换为int，所以这里要把int转的01串补成8位
			String s = Integer.toBinaryString(value);
			
			for(int i = s.length() ; i < 8; i++) {
				strbuf.append("0").append(s);   //左补0   ??buffer
				str = strbuf.toString();
				}			
		}		
	*/	
/*		
		int num = 0;   //保存正在huff解码的串长度
		HuffNode node = chuffRoot;
		//HuffNode last_node = root;
		String decode_str ="";

		StringBuilder decode_strb = new StringBuilder();  //字符串拼接：单线程stringBuilder > stringBuffer(线程安全）>string	
		StringBuffer strbuf = new StringBuffer();   //存补了0的拼接字符串
		
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(fileName));	//bufferReader读取文件-> 2bytes, 还是用inputstream
		BufferedOutputStream output = new BufferedOutputStream( new FileOutputStream(destFile));
		
	//	String delete_buf;
		
		for( int i = 0; i < fileLen - 2; i++) {    //用文件长度控制读取： 以免结束符-1 和 文件中的-1 冲突
			
			int value = input.read();    //input.read() & 0xff;   read byte  直接转换为int是有符号扩展，如果第一位是1，则最后读出来是个负数
            //0xff默认用int来保存，所以首先将 b[0] 强制转化为 int 类型表示，b[0] 的数值为-128，所以其二进制表示为11111111 11111111 11111111 10000000
            //                                                                            0xff的二进制表示为 00000000 00000000 00000000 11111111
                                                                                                                                                                                 这两个数做 & 运算后，得到 00000000 00000000 00000000 10000000，其表示的值为128
			Integer a = new Integer(value);
			// s 补0后拼接 
			strbuf.append(addZeroForNum( Integer.toBinaryString(a), 8));     //   toBinaryString 的第一个字符不能是0，会被删掉，所以要补0   
			
	//		last_num = value;     //最后一个整的8bits是加了0的源文件的最后，单独出来的是压入的0的个数
		
			int flag = 0;       //判断读到根节点的标志	
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
				num++;           //数出编码长度	
				if(flag == 1) {     //如果已成功获得一个节点
				
					output.write((byte)node.getIndex());
				//	decode_strb.append((byte)node.getIndex());  // 截取HuffNode.index(int)的后八位(byte)恢复
					                                             // 强制转换为byte :有符号byte
					strbuf.delete(0, num - 1);    //删除已经解码的
					flag = 0;                 //我tm竟然因为忘记置0调了几个小时
					num = 0;    //重置0
					node = chuffRoot;   //重置根节点
				}		
				
			}
	//		delete_buf = strbuf.toString();
		}
		
		Integer last_s = new Integer(input.read());
		int last_n = input.read();
		strbuf.append(addZeroForNum(Integer.toBinaryString(last_s),8).substring(last_n));   //把减去0 的最后一个字符串加到后面
		
		int flag = 0;       //判断读到根节点的标志	
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
			num++;           //数出编码长度	
			if(flag == 1) {     //如果已成功获得一个节点
				output.write((byte)node.getIndex());
				//decode_strb.append((byte)node.getIndex() + "");  // 截取treenode(int)的后八位(byte)恢复
				// 强制转换为byte :有符号byte）
				strbuf.delete(0, num-1);    //删除已经解码的
			//	delete_buf = strbuf.toString();
				num = 0;    //重置0
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
		//因为compress中是先分为8bits一份，再转换为int，所以这里要把int转的01串补成8位		
		String s = Integer.toBinaryString(a);
		last_str = s;   //保存上一个字符串(未补0
		strbuf.append(addZeroForNum(s, 8));   // s 补0后拼接
		last_num = value;     //最后一个整的8bits是加了0的源文件的最后，单独出来的是压入的0的个数
		value = input.read();
	//	System.out.println(str);
	    }
	input.close();
	//？ 
	
	System.out.println("Decoding: End-read.");	
    }
	
    catch(IOException e){
		
	}	
	
	long endRead = System.currentTimeMillis();
	System.out.println("Decoding...  Read Time :" + (endRead-startRead));
	
	
	System.out.println("last_num:" + last_num);
	System.out.println("last_str:" + last_str);
	str = strbuf.toString();
	str = str.substring(0,str.length() - last_num);   //去掉后面的0(last_num 个
	System.out.println("str.length:" + str.length());
	
//Huffm解码	
	
//	int a =(int)(str.charAt(num) - 48);
/*	HuffNode node = null;
	HuffNode last_node = root;
	String decode_str ="";

	StringBuilder decode_strb = new StringBuilder();  //字符串拼接：单线程stringBuilder > stringBuffer(线程安全）>string	
/*	
	long startHuff = System.currentTimeMillis();
	
	
	//原方法
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
			num++;           //数出编码长度
			if(num > str.length() - 1) 
				break;			
		}
		decode_strb.append((char)last_node.getIndex()).append("");   //为什么要加“”？
	    str = str.substring(num);   //将已经解码的串取出
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
			num++;           //数出编码长度
			if(num > str.length() - 1) 
				break;			
		}
		decode_strb.append((char)last_node.getIndex());   //为什么要加“”？
	    str = str.substring(num);   //将已经解码的串取出
	    num = 0;
	    last_node = root;
	}
*/
		
	
	/*把字符串打印入文件
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
	//打印解码后code
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
	


