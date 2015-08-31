/**
 * 
 *  THINK VPL is a visual programming language and integrated development environment for that language
 *  Copyright (C) 2015 Quinn Freedman
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General  License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General  License for more details.
 *
 *  You should have received a copy of the GNU General  License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  For more information, visit the THINK VPL website or email the author at
 *  quinnfreedman@gmail.com
 * 
 */

package think;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import think.Variable.DataType;

class Compiler{
	
	private static int indent;
	private static String ln;
	private static ArrayList<String> imports = new ArrayList<String>();
	
	private static void addImport(String s){
		imports.add("import "+s+";");
	}
	
	static void compile() throws Exception{
		
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File(Main.SAVE_DIR));
		FileFilter filter = new FileNameExtensionFilter("Java File","java");
	    jfc.setFileFilter(filter);
		int result = jfc.showSaveDialog(Main.window);
		if (result == JFileChooser.APPROVE_OPTION) {
		    File selectedFile = jfc.getSelectedFile();
		    Out.pln("Selected file: " + selectedFile.getAbsolutePath());
		    if(!selectedFile.getName().endsWith(".java")){
		    	selectedFile = new File(selectedFile.getAbsolutePath()+".java");
		    }
		    
		    Out.pln("Compiling");
		    
		    ln = System.getProperty("line.separator");
		    
		    String[] simpleName = selectedFile.getName().replace(".", "-").split("-");
		    
			ArrayList<String> lines = getText(simpleName[0]);
			
			Out.pln("Finished Compiling");
			
			Out.pln("Creating file "+selectedFile.toString()+"...");
			PrintWriter writer;
			try {
				writer = new PrintWriter(selectedFile, "UTF-8");
				for(String s : imports){
					writer.println(s);
				}
				for(String s : lines){
					writer.println(s);
				}
				writer.close();
				Out.pln("Succeeded");
			} catch (FileNotFoundException e) {
				Out.pln("FAILED: File not found: "+e.getMessage());
			} catch (UnsupportedEncodingException e) {
				throw e;
			}
		    
		}
		
	}

	private static ArrayList<String> getText(String name) {
		ArrayList<String> lines = new ArrayList<String>();
		indent = 0;
		
		lines.add("/**");
		lines.add(" *  Created with THINK version "+Main.VERSION_ID);
		lines.add(" *  ThinkVPL.org");
		lines.add(" */");
		lines.add("class "+name+" {");
		
		indent++;
		
		for(Variable v : Main.mainBP.getVariables()){
			if(v instanceof VArray){
				
			}else if(v instanceof VInstance){
				
			}else{
				String quote = ((v.dataType == Variable.DataType.STRING) ? "\"" : ((v.dataType == Variable.DataType.CHARACTER) ? "\'" : ""));
				lines.add(getIndent()+getJavaName(v.dataType)+" "+v.getID()+" = "+quote+v.valueField.getText()+quote);
			}
		}
		
		lines.add(getIndent()+"public static void main(String[] args) {");
		
		lines.addAll(getContinuousWireText(Main.entryPoint.getOutputNodes().get(0)));
		
		lines.add(getIndent()+"}");
		
		indent--;
		
		lines.add("}");
		return lines;
	}
	
	private static ArrayList<String> getContinuousWireText(Node n){
		return getContinuousWireText(n, true);
	}
	
	private static ArrayList<String> getContinuousWireText(Node n, boolean indented){
		if(indented)
			indent++;
		ArrayList<String> lines = new ArrayList<String>();
		
		Executable current = n.children.isEmpty() ? null : (Executable) n.children.get(0).parentObject;
		
		while(current != null){
			String line = getIndent()+getFunctionCall(current,false);
			if(!(line.endsWith("}") || line.endsWith(ln)))
				line += ";";
			lines.add(line);
			if(line.endsWith("}") || line.endsWith(ln)){
				break;
			}
			current = getNext(current);
		}
		
		if(indented)
			indent --;
		return lines;
	}
	
	private static String getFunctionCall(Executable ex) {
		return getFunctionCall(ex,true);
	}
	
	private static String getFunctionCall(Executable ex, boolean isCalledAsArguement) {
		String output = null;
		
		if(ex instanceof Constant){
			String quote = "";
			String suffix = "";
			switch (((Constant) ex).dt) {
			case ARRAY:
				return "ERROR";//TODO
			case BYTE:
				break;
			case CHARACTER:
				quote = "\'";
				break;
			case DOUBLE:
				suffix = "d";
				break;
			case FLOAT:
				suffix = "f";
				break;
			case INTEGER:
				break;
			case LONG:
				suffix = "L";
				break;
			case STRING:
				quote = "\"";
				break;
			default:
				break;
			
			}
			
			output = quote+((Constant) ex).editor.getText()+suffix+quote;
		}else if(ex instanceof PrimitiveFunction){
			if(ex.getClass().getSimpleName().equals("Get")){
				output = ((PrimitiveFunction) ex).parentVariable.getID();
			}else if(ex.getClass().getSimpleName().equals("Set")){
				output = ((PrimitiveFunction) ex).parentVariable.getID()+" = "
						+getFunctionCall((Executable) ex.getInputNodes().get(1).parents.get(0).parentObject);
			}else if(ex.getClass().getSimpleName().equals("Multiply_By")){
				output = ((PrimitiveFunction) ex).parentVariable.getID()+" *= "
						+getFunctionCall((Executable) ex.getInputNodes().get(1).parents.get(0).parentObject);
			}else if(ex.getClass().getSimpleName().equals("Add_To")){
				output = ((PrimitiveFunction) ex).parentVariable.getID()+" = "
						+getFunctionCall((Executable) ex.getInputNodes().get(1).parents.get(0).parentObject);
			}else if(ex.getClass().getSimpleName().equals("Increment")){
				output = ((PrimitiveFunction) ex).parentVariable.getID()+"++";
			}else{
				output = "ERROR";//TODO handle arrays, objects
			}
		}else if(ex instanceof Binop){
			if(ex instanceof Logic.Not){
				output = "!("+getFunctionCall((Executable) ex.getInputNodes().get(0).parents.get(0).parentObject)+")";
			}else{
				Executable input1 = (Executable) ex.getInputNodes().get(0).parents.get(0).parentObject;
				Executable input2 = (Executable) ex.getInputNodes().get(1).parents.get(0).parentObject;
				
				String str1 = getFunctionCall(input1);
				String str2 = getFunctionCall(input2);
				
				if(str1.contains(" "))
					str1 = "("+str1+")";
				if(str2.contains(" "))
					str2 = "("+str1+")";
				
				output = str1+" "+((Binop) ex).getJavaBinop()+" "+str2;
			}
			
		}else if(ex instanceof Cast){
			
			String functionCall = getFunctionCall((Executable) ex.getInputNodes().get(0).parents.get(0).parentObject);
			
			output = "("+getJavaName(((Cast) ex).getOutput())+") "+(functionCall.contains(" ") ? "(" : "")+functionCall+(functionCall.contains(" ") ? ")" : "");
			
		}else if(ex instanceof FlowControl.Branch){
			output = "if ("
					+getFunctionCall((Executable) ex.getInputNodes().get(1).parents.get(0).parentObject)
					+") {"+ln;
			for(String s : getContinuousWireText(ex.getOutputNodes().get(0))){
				output += s+ln;
			}
			ArrayList<String> lines2 = getContinuousWireText(ex.getOutputNodes().get(1));
			if(!lines2.isEmpty()){
				output += (getIndent()+"} else {"+ln);
			}
			for(String s : lines2){
				output += s+ln;
			}
		}else if(ex instanceof FlowControl.For){
			if(isCalledAsArguement){
				output = "i";
			}else{
				output = "for (int i = 0; i < "//TODO add index property to each for object
						+getFunctionCall((Executable) ex.getInputNodes().get(1).parents.get(0).parentObject)
						+"; i++) {"+ln;
				for(String s : getContinuousWireText(ex.getOutputNodes().get(0))){
					output += s+ln;
				}
				output += getIndent()+"}"+ln;
				ArrayList<String> lines2 = getContinuousWireText(ex.getOutputNodes().get(2), false);
				for(String s : lines2){
					output += s+ln;
				}
			}
		}else if(ex instanceof FlowControl.AdvancedFor){
			
		}else if(ex instanceof FlowControl.Sequence){
			
		}else if(ex instanceof FlowControl.While){
			output = "while ("
					+getFunctionCall((Executable) ex.getInputNodes().get(1).parents.get(0).parentObject)
					+") {"+ln;
			for(String s : getContinuousWireText(ex.getOutputNodes().get(0))){
				output += s+ln;
			}
			output += getIndent()+"}"+ln;
			//indent--;
			ArrayList<String> lines2 = getContinuousWireText(ex.getOutputNodes().get(1), false);
			for(String s : lines2){
				output += s+ln;
			}
		}else if(ex instanceof FlowControl.Wait){
			output = getIndent()+"try {"+ln;
			indent++;
			output += getIndent()+"Thread.sleep((long)("
				+ getFunctionCall((Executable) ex.getInputNodes().get(1).parents.get(0).parentObject)
				+ ");"+ln;
			indent--;
			output += "} catch (InterruptedException e) {e.printStackTrace();}";
		}else if(ex instanceof Console.getStr){
			addImport("java.util.Scanner");
			if(((Console.getStr) ex).getDataType() == Variable.DataType.DOUBLE){
				output = "(new Scanner(System.in)).nextDouble()";
			}else{
				output = "(new Scanner(System.in)).nextLine()";
			}
		}else{
			
			output = ((ex instanceof JavaKeyword) ? ((JavaKeyword) ex).getJavaKeyword() : ex.getClass().getSimpleName())+"(";//TODO handle other classes
			
			for(Node n : ex.getInputNodes()){
				if(!n.parents.isEmpty() && n.dataType != Variable.DataType.GENERIC){
					output += getFunctionCall((Executable) n.parents.get(0).parentObject)+", ";
				}
			}
			
			if(output.length() >= 2 && output.charAt(output.length()-1) == ' '){
				output = output.substring(0, output.length()-2);
			}
			
			output += ")";
		}
		
		Out.pln(output);
		if(output.contains(ln)){
			//output += getIndent()+"}";
		}
		return output;
	}

	private static Executable getNext(Executable current) {
		
		if(!current.getOutputNodes().isEmpty()){
			Node outputNode = current.getOutputNodes().get(0);
			if(outputNode.dataType == Variable.DataType.GENERIC && !outputNode.children.isEmpty()){
				return (Executable) outputNode.children.get(0).parentObject;
			}
		}
		return null;
	}

	private static String getJavaName(DataType dt){
		switch(dt){
		case CHARACTER:
			return "char";
		case INTEGER:
			return "int";
		case STRING:
			return "String";
		default:
			return dt.toString().toLowerCase();
		}
	}
	private static String getIndent(){
		String output = "";
		for(int i = 0; i < indent; i++){
			output += "    ";
		}
		
		return output;
	}
	
}