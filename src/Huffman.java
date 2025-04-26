import java.util.PriorityQueue;
import java.io.*;

public class Huffman {

    private static int R=256;

    private static class HuffmanNode implements Comparable<HuffmanNode> {
        char ch;
        int freq;
        HuffmanNode left,right;

        public HuffmanNode(char ch, int freq, HuffmanNode left, HuffmanNode right) {
            this.ch = ch;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        boolean isLeaf(){
            return left==null && right==null;
        }

        @Override
        public int compareTo(HuffmanNode o) {
            return Integer.compare(this.freq,o.freq);
        }
    }

    private static void compress(String inputPath,DataOutputStream output) throws IOException{
        int[] freq= new int[R];
        int size=0;
        BufferedInputStream input=new BufferedInputStream(new FileInputStream(inputPath));
        int b;
        while((b=input.read())!=-1){
            size++;
            freq[b]++;
        }


        HuffmanNode root=buildTree(freq);

        String[] st=new String[R];
        buildCode(st,root,"");

        output.writeInt(size);
        writeTree(output,root);

        input=new BufferedInputStream(new FileInputStream(inputPath));


        int buffer=0;
        int bitCount=0;
        while((b=input.read())!=-1){
            String code=st[b & 0xFF];
            for(char c:code.toCharArray()){
                buffer <<=1;
                if(c=='1'){
                    buffer |=1;
                }
                bitCount++;
                if(bitCount==8){
                    output.write(buffer);
                    buffer=0;
                    bitCount=0;
                }
            }
        }
        if(bitCount>0){
            buffer <<=(8-bitCount);
            output.write(buffer);
        }

    }
    private static HuffmanNode buildTree(int[] freq){
        PriorityQueue<HuffmanNode> pq=new PriorityQueue<>();
        for(char c=0;c<R;c++){
            if(freq[c]>0){
                pq.add(new HuffmanNode(c,freq[c],null,null));
            }
        }
        while(pq.size()>1){
            HuffmanNode left=pq.remove();
            HuffmanNode right=pq.remove();
            HuffmanNode parent=new HuffmanNode('\0',left.freq+right.freq,left,right);
            pq.add(parent);
        }
        return pq.remove();
    }

    public static void writeTree(DataOutputStream output,HuffmanNode x) throws IOException{
        if(x.isLeaf()){
            output.writeByte(1);
            output.writeChar(x.ch);
            return;
        }
        output.writeByte(0);
        writeTree(output,x.left);
        writeTree(output,x.right);
    }


    private static void buildCode(String[] st, HuffmanNode x,String s){
        if(!x.isLeaf()){
            buildCode(st,x.left,s+'0');
            buildCode(st,x.right,s+'1');
        }else{
            st[x.ch]=s;
        }
    }

    private static void expand(DataInputStream input,DataOutputStream output) throws IOException{
        int size=input.readInt();
        HuffmanNode root=readTree(input);

        int bytesRead=0;
        int currentByte=0;
        int bitsLeft=0;
        HuffmanNode current=root;

        while(bytesRead < size){
            if(bitsLeft==0){
                currentByte=input.read();
                bitsLeft=8;
            }
            int bit=(currentByte >> (bitsLeft-1)) & 1;
            bitsLeft--;

            if(bit==0){
                current=current.left;
            }else{
                current=current.right;
            }

            if(current.isLeaf()){
                output.write(current.ch);
                bytesRead++;
                current=root;
            }
        }
    }
    private static HuffmanNode readTree(DataInputStream input) throws IOException{
        int flag=input.readByte();
        if(flag==1){
            char ch=input.readChar();
            return new HuffmanNode(ch,-1,null,null);
        }else if(flag==0){
            HuffmanNode left=readTree(input);
            HuffmanNode right=readTree(input);
            return new HuffmanNode('\0',-1,left,right);
        }else{
            throw new IOException("Invalid tree format");
        }
    }


    public static void main(String[] args) {
        if(args.length < 3){
            System.out.println("Incorect number of arguments");
            return;
        }
        try{
            DataInputStream input=new DataInputStream(new FileInputStream(args[1]));
            DataOutputStream output=new DataOutputStream(new FileOutputStream(args[2]));

            long startTime=System.nanoTime();

            if(args[0].equals("-c")){
                compress(args[1],output);

                File inputFile = new File(args[1]);
                File outputFile = new File(args[2]);
                long inputSize = inputFile.length();
                long outputSize = outputFile.length();
                double compressionRate=(double) outputSize / inputSize * 100.0;
                System.out.println("Compression rate: "+compressionRate);

            }else if(args[0].equals("-d")){
                expand(input,output);
            }else throw new IllegalArgumentException("Illegal command line argument");

            long endTime=System.nanoTime();
            double elapsedTime = (endTime-startTime) / 1000000000.0;
            System.out.println("Time in seconds: "+elapsedTime);

        }catch(IOException e){
            System.out.println(e.getMessage());
        }catch(IllegalArgumentException e){
            System.out.println(e.getMessage());
        }
    }
}
