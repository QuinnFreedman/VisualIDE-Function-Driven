import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JPanel;

public class Node extends JPanel implements MouseListener, MouseMotionListener{
	private static final long serialVersionUID = 1L;
	
	static Node currentlyDragging;
	ArrayList<Node> parents = new ArrayList<Node>();
	ArrayList<Node> children = new ArrayList<Node>();
	VObject parentObject;
	Direction facing;
	NodeType type;
	boolean canHaveMultipleInputs = true;
	boolean isHover = false;
	public Variable.DataType dataType;
	protected Dimension size = new Dimension(15,15);
	Node(NodeType type,VObject parentObj,boolean mi){
		this.canHaveMultipleInputs = mi;
		this.setOpaque(false);
		this.addMouseListener(this);
		this.facing = (type == NodeType.SENDING || type == NodeType.INHERITANCE_SENDING) ? Direction.SOUTH : Direction.NORTH;
		this.type = type;
		this.parentObject = parentObj;
	}
	Node(NodeType type,VObject parentObj){
		this(type,parentObj,false);
	}
	Node(NodeType type, VObject parentObj, Variable.DataType dt){
		this(type,parentObj,false);
		this.dataType = dt;
	}
	public Node(NodeType type, VObject parentObj, Variable.DataType dt,
			boolean b) {
		this(type,parentObj,b);
		this.dataType = dt;
	}
	public void onConnect(){
		//override in subclass
	}
	public void onDisconnect(){
		//override in subclass
	}
	private static void clearChildren(Node nodeToClear){
		if(nodeToClear.canHaveMultipleInputs == false){
			nodeToClear.onDisconnect(); //TODO be more specific? ie call later in the method only if connection is actually removed
			Iterator<Curve> itr = Main.curves.iterator();
			while(itr.hasNext()){
				Curve c = itr.next();
				Node node;
				if(c.nodes[0] == nodeToClear){
					node = c.nodes[1];
				}else if(c.nodes[1] == nodeToClear){
					node = c.nodes[0];
				}else{
					continue;
				}
				
				if(node.type == Node.NodeType.RECIEVING){
					node.parents.remove(nodeToClear);
					nodeToClear.children.remove(node);
				}else{
					nodeToClear.parents.remove(node);
					node.children.remove(nodeToClear);
				}
				itr.remove();
				
			}
		}
	}
	
	public static ArrayList<ArrayList<Variable.DataType>> complement(ArrayList<Variable.DataType> A, ArrayList<Variable.DataType> B){
		ArrayList<Variable.DataType> sourceList = new ArrayList<Variable.DataType>(A);
		ArrayList<Variable.DataType> destinationList = new ArrayList<Variable.DataType>(B);
		
		System.out.println("A : "+A);
		System.out.println("B : "+B);
		System.out.println("sourceList : "+sourceList);
		System.out.println("destinationList : "+destinationList);
		
		sourceList.removeAll(B);
		destinationList.removeAll(A);
		
		return new ArrayList<ArrayList<Variable.DataType>>(Arrays.asList(sourceList,destinationList));
	}
	
	public static void connect(Node A, Node B){
		B.parents.add(A);
		A.children.add(B);
		Main.curves.add(new Curve(A,B));
		A.onConnect();
		B.onConnect();
	}
	
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.fillArc(0, 0, 14, 14, 0, 360);
		if(this.dataType != Variable.DataType.GENERIC){
			g.setColor(Main.colors.get(this.dataType));
			g.fillArc(this.size.width/2 - 4, this.size.height/2 - 4, 8, 8, 0, 360);
		}
		if(isHover){
			g2.setColor(new Color(1f,1f,1f,0.5f));
			g.fillArc(0, 0, 14, 14, 0, 360);
		}
			
	}
	/*@Override
	public Dimension getSize(){
		return new Dimension(300,300);
	}*/
	@Override
	public Dimension getPreferredSize(){
		return size;
	}
	@Override
	public void mouseClicked(MouseEvent arg0) {
		if(Main.altPressed){
			clearChildren(this);
		}
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
		if(currentlyDragging != null && canConnect(currentlyDragging,this)){
			this.isHover = true;
		}
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
		this.isHover = false;
		this.repaint();
	}
	@Override
	public void mousePressed(MouseEvent arg0) {
		currentlyDragging = this;
		this.addMouseMotionListener(this);
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		currentlyDragging = null;
		this.removeMouseMotionListener(this);
		Point mouse = getLocationOnPanel(e);
		Rectangle rect = new Rectangle();
		for(Node node : Main.nodes){
			rect = new Rectangle(getLocationOnPanel(node),new Dimension(node.getWidth(),node.getHeight()));
			if(rect.contains(mouse) && canConnect(this,node)){
				
				clearChildren(this);
				clearChildren(node);
				Node A;
				Node B;
				if(node.type == NodeType.RECIEVING && this.type == NodeType.SENDING){
					A = this;
					B = node;
				}else{
					A = node;
					B = this;
				}
				if(this.dataType == node.dataType){
					connect(A,B);
				}else if(Cast.isCastable(A.dataType,B.dataType)){
					Main.objects.add(new Cast(A,B));
				}
				break;
				
			}
		}
		
		Main.panel.repaint();
	}
	
	private static boolean canConnect(Node A, Node B){
		if(A.parentObject == B.parentObject){
			return false;
		}
		if((A.type == NodeType.SENDING && B.type == NodeType.RECIEVING) ||
			(A.type == NodeType.RECIEVING && B.type == NodeType.SENDING))
		{
			if(A.type == NodeType.RECIEVING){
				if(A.parents.contains(B) || B.children.contains(A)){
					return false;
				}
			}else{
				if(B.parents.contains(A) || A.children.contains(B)){
					return false;
				}
			}
			if(A.dataType == B.dataType){
				return true;
			}else if(Cast.isCastable(A.dataType,B.dataType)){
				if(Cast.isCastable(A.dataType, B.dataType)){
					return true;
				}else{
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Main.mousePos = getLocationOnPanel(e);
		Main.panel.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		/*Main.mousePos.x = e.getLocationOnScreen().x-this.getLocationOnScreen().x;
		Main.mousePos.x = e.getLocationOnScreen().y-this.getLocationOnScreen().y;
		Main.panel.repaint();*/
	}
	public static Point getLocationOnPanel(Component c){
		return new Point(c.getLocationOnScreen().x-Main.panel.getLocationOnScreen().x,c.getLocationOnScreen().y-Main.panel.getLocationOnScreen().y);
	}
	public static Point getLocationOnPanel(MouseEvent e){
		return new Point(e.getLocationOnScreen().x-Main.panel.getLocationOnScreen().x,e.getLocationOnScreen().y-Main.panel.getLocationOnScreen().y);
	}
	public enum Direction{
		NORTH,SOUTH,EAST,WEST
	}
	public enum NodeType{
		SENDING,RECIEVING,INHERITANCE_SENDING,INHERITANCE_RECIEVING
	}
	public enum NodeStyle{
		INVISIBLE,VISIBLE
	}
}