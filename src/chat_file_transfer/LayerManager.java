package chat_file_transfer;


import java.util.ArrayList;
import java.util.StringTokenizer;

public class LayerManager {
	
	private class Node{
		private String token;
		private Node next;
		public Node(String input){
			this.token = input;
			this.next = null;
		}
	}

	private Node listHead;
	private Node listTail;
	
	private int topIndex;
	private int layerCount;

	private ArrayList<BaseLayer> stack = new ArrayList<BaseLayer>();
	private ArrayList<BaseLayer> layerlist = new ArrayList<BaseLayer>() ;
	

	public LayerManager(){
		layerCount = 0;
		listHead = null;
		listTail = null;
		topIndex = -1;
	}
	
	public void AddLayer(BaseLayer layer){
		layerlist.add(layerCount++, layer);
	}
	
	
	public BaseLayer GetLayer(int index){
		return layerlist.get(index);
	}
	
	public BaseLayer GetLayer(String name){
		for( int i=0; i < layerCount; i++){
			if(name.compareTo(layerlist.get(i).getLayerName()) == 0)
				return layerlist.get(i);
		}
		return null;
	}
	
	public void ConnectLayers(String pcList){
		MakeList(pcList);
		LinkLayer(listHead);		
	}

	private void MakeList(String pcList){
		StringTokenizer tokens = new StringTokenizer(pcList, " ");
		
		for(; tokens.hasMoreElements();){
			Node pNode = AllocNode(tokens.nextToken());
			AddNode(pNode);
		}	
	}

	private Node AllocNode(String pcName){
		Node node = new Node(pcName);
		return node;				
	}
	
	private void AddNode(Node node){
		if(listHead == null){
			listHead = listTail = node;
		}else{
			listTail.next = node;
			listTail = node;
		}
	}

	private void Push (BaseLayer layer){
		stack.add(++topIndex, layer);
	}

	private BaseLayer Pop(){
		BaseLayer layer = stack.get(topIndex);
		stack.remove(topIndex);
		topIndex--;
		
		return layer;
	}
	
	private BaseLayer Top(){
		return stack.get(topIndex);
	}
	
	private void LinkLayer(Node node){
		BaseLayer layer = null;
		
		while(node != null){
			if( layer == null)
				layer = GetLayer (node.token);
			else{
				if(node.token.equals("("))
					Push (layer);
				else if(node.token.equals(")"))
					Pop();
				else{
					char cMode = node.token.charAt(0);
					String pcName = node.token.substring(1, node.token.length());
					
					layer = GetLayer (pcName);
					
					switch(cMode){
					case '*':
						Top().setUpperUnderLayer( layer );
						break;
					case '+':
						Top().setUpperLayer( layer );
						break;
					case '-':
						Top().setUnderLayer( layer );
						break;
					}					
				}
			}
			node = node.next;
		}
	}
}
