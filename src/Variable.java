import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public class Variable extends SidebarItem implements DocumentListener, ContainsChildFunctions{
	InputPane valueField;
	DataType dataType;
	VariableData varData;
	static final Border bodyPadding = new EmptyBorder(5,10,5,10);
	protected ArrayList<PrimitiveFunction> functions = new ArrayList<PrimitiveFunction>();//TODO use classes
	private ArrayList<PrimitiveFunction> children = new ArrayList<PrimitiveFunction>();
	protected PrimitiveChildPicker childPicker;
	public void removeChild(PrimitiveFunction pf){
		children.remove(pf);
	}
	public void addChild(PrimitiveFunction pf){
		children.add(pf);
	}
	public void clearChildren(){
		Iterator itr = this.children.iterator();
		while(children.size() > 0) {
			children.get(0).delete();
		}
		if(childPicker != null)
			childPicker.delete();
	}
	private Variable getThis(){
		return this;
	}
	Variable(){
		super();
		
		this.type = Type.VARIABLE;
		
		//TODO if is object w/ children, add all children to body, set body visible
		
		//JPanel body = new JPanel(new FlowLayout());
		
		nameField.getDocument().addDocumentListener(new NameDocListener(this));
		
		valueField = new InputPane(this);
		valueField.setColumns(5);
		valueField.getDocument().addDocumentListener(this);
		header.add(valueField);
		fields.add(valueField);
		
		JRadioButton drag = new JRadioButton();
		drag.setFocusable(false);
		drag.setSelected(true);
		drag.addMouseListener(new MouseListener(){{
			
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				drag.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if(getThis().dataType != null){
					drag.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					Point p = Node.getLocationOnPanel(e);
					if(getThis().childPicker != null){
						childPicker.delete();
					}
					if(p.x > 0 && p.y > 0 && p.x < Main.panel.getWidth() && p.y < Main.panel.getHeight()){
						PrimitiveChildPicker childPicker = new PrimitiveChildPicker(getThis(), p);
						Main.objects.add(childPicker);
						getThis().childPicker = childPicker;
					}
				}
			}
			
		});
		header.add(drag);
		
	}
	
	protected void setValue(String s){
		//Overwritten in subclasses
	}
	protected void setChildTexts(String s){
		for(PrimitiveFunction child : children){
			child.setText(s);
		}
	}
	
    @Override
    public void removeUpdate(DocumentEvent e) {
    	try {
			setValue(e.getDocument().getText(0, e.getDocument().getLength()));
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
    	try {
			setValue(e.getDocument().getText(0, e.getDocument().getLength()));
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    	try {
			setValue(e.getDocument().getText(0, e.getDocument().getLength()));
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
    }
    static class NameDocListener implements DocumentListener{

    	Variable var;
    	
    	NameDocListener(Variable var){
    		this.var = var;
    	}
    
		@Override
	    public void removeUpdate(DocumentEvent e) {
	    	try {
				var.setChildTexts(e.getDocument().getText(0, e.getDocument().getLength()));
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
	    }

	    @Override
	    public void insertUpdate(DocumentEvent e) {
	    	try {
		    	var.setChildTexts(e.getDocument().getText(0, e.getDocument().getLength()));
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
	    }

	    @Override
	    public void changedUpdate(DocumentEvent e) {
	    	try {
	    		var.setChildTexts(e.getDocument().getText(0, e.getDocument().getLength()));
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
	    }
    	
    }
    public enum DataType{
    	BOOLEAN,BYTE,SHORT,INTEGER,FLOAT,DOUBLE,LONG,CHARACTER,STRING,GENERIC;
    	
    }
	@Override
	public ArrayList<PrimitiveFunction> getFunctions() {
		return functions;
	}
}