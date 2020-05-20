package hjj;

public class HuffNode {

	 private int data;
	 private int index;
	 private HuffNode left ;
	 private HuffNode right ;
	 
	 HuffNode (int data) {
		 this.data = data;
	 }
	 
	 HuffNode (int data, int index) {
		 this.data = data;
		 this.index =index;
	 }
	 
	 public HuffNode () {
		 
	 }
	 
	 public int getData() {
		 return data;
	 }
	 
	 public void setData(int data) {
		 this.data = data;
	 }
	 
	 public int getIndex() {
		 return this.index;
	 }
	 public void setIndex(int index) {
		 this.index = index;
	 }
	 public HuffNode getLeft() {
		 return left;
	 }
	 public HuffNode getRight() {
		 return right;
	 }
	 public void setLeft(HuffNode left) {
		 this.left = left;
	 }
	 public void setRight(HuffNode right) {
		 this.right = right;
	 }
}
