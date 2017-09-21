import java.io.*;
import java.util.*;


class Symbol{
    String symbol;
    int module;
    int addr;
    int error;
}

class Code extends Output{
    Output root;
    String opcode;
    String addr;
    String type;
    String error;
    Code() {
        root = null;
    }
}



class Output{
    int value = 0;
}

public class Linker {

    static int get_addr2(int i, int addr1, Map<Integer, Integer> ModuleCounter){
        for(int n =0; n<i; n++){
            addr1 += ModuleCounter.get(n);
        }
        return addr1;
    }


    static int get_module_number(int i, Map<Integer, Integer> ModuleCounter){
        int module_number = 0;
        for(int n =0; n<i+1; n++){
            module_number += ModuleCounter.get(n);
        }
        return module_number;
    }




    public static void main(String args[]){



        try {

            System.out.print("Enter the file name with extension : ");

            Scanner input = new Scanner(System.in);

            File file = new File(input.nextLine());

            input = new Scanner(file);



            if (input.hasNextInt()){

                Map<Integer,Map> Module = new HashMap<Integer, Map>();
                Map<Integer,Integer> ModuleCounter = new HashMap<Integer,Integer>();

                //Map<String,Node> map = new HashMap<String,Node>();


                Map<String,Symbol> sym = new HashMap<String, Symbol>();
                //ArrayList<Symbol> SymbolTable = new ArrayList<Symbol>();


                //process_one

                System.out.println("Symbol Table");
                int module = input.nextInt();
                for (int i=0; i<module; i++){
                    //def line
                    int def = input.nextInt();
                    if (def != 0){
                        for (int j=0; j<def; j++) {
                            String symbol = input.next();

                            Symbol defsym = new Symbol();
                            defsym.symbol = symbol;
                            int a = input.nextInt();

                            if (sym.containsKey(symbol)){
                                //error type 1
                                defsym.module = sym.get(symbol).module;
                                defsym.addr = sym.get(symbol).addr;
                                defsym.error = 1;
                            }else{
                                for (Integer value : ModuleCounter.values()) {
                                    a += value;
                                }
                                defsym.module = i;
                                defsym.addr = a;
                            }
                            sym.put(symbol, defsym);


                        }
                    }


                    //refer line
                    Map<String,Symbol> map2 = new HashMap<String, Symbol>();

                    int ref = input.nextInt();
                    for (int j=0; j<ref; j++) {
                        String symbol = input.next();
                        int a = input.nextInt();
                        Symbol refersym = new Symbol();
                        refersym.addr = a;
                        refersym.module = i;

                        map2.put(symbol,refersym);
                        //System.out.println(symbol+":"+i+":"+a);



                    }
                    //Code line
                    Map<Integer,Code> map1 = new HashMap<Integer, Code>();

                    int Index = input.nextInt();


                    for (int k=0; k<Index; k++){

                        String a = Integer.toString(input.nextInt());
                        Code code = new Code();
                        code.opcode = a.substring(0,1);
                        code.type = a.substring(4);
                        code.addr = a.substring(1,4);

                        //System.out.println(k+":"+code.opcode+code.addr+code.type);
                        map1.put(k,code);


                    }

                    Map<String,Map> map = new HashMap<String,Map>();
                    ModuleCounter.put(i,Index);
                    map.put("map1",map1);
                    map.put("map2",map2);
                    Module.put(i,map);

                }

                //Print Symbol Table

                //check error 3
                for (Map.Entry<String,Symbol> temp : sym.entrySet()){
                    int a = temp.getValue().addr;
                    int b = get_module_number(temp.getValue().module,ModuleCounter);
                    //System.out.println("a"+a);
                    //System.out.println("b"+temp.getValue().addr);
                    if(a>b-1){
                        //System.out.println("Error Type 3");
                        temp.getValue().error = 3;
                        temp.getValue().addr = get_module_number((temp.getValue().module-1),ModuleCounter);
                    }
                }




                for (Map.Entry<String,Symbol> temp : sym.entrySet()){
                    int a = temp.getValue().addr;
                    System.out.print(temp.getKey()+"="+a);
                    switch(temp.getValue().error){
                        case 1:
                            System.out.println("    Error: This variable is multiply defined; first value used.");
                            break;
                        case 3:
                            System.out.println("    Error: The definition of "+temp.getKey()+" is outside module "+
                                    temp.getValue().module+";   zero (relative) used.");
                            break;
                        default:
                            System.out.println();
                            break;
                    }
                }

                /*
                for (Map<Integer,Map> v: Module.values()){
                    Map<Integer,Code> t = v.get("map1");
                    for (Map.Entry<Integer,Code> en: t.entrySet()){
                        System.out.println(en.getKey()+":"+en.getValue().opcode+en.getValue().addr+en.getValue().type);
                    }
                }
                */





                System.out.println();
                System.out.println("Memory Map");




                //process_two
                Map<Integer,Output> out = new HashMap<Integer, Output>();

                for(Map.Entry<Integer,Map> entry0 : Module.entrySet()){
                    int i = entry0.getKey();
                    //System.out.println("--------------");

                    Map<String,Map> temp_map = entry0.getValue();
                    Map<Integer,Code> temp_map1 = temp_map.get("map1");
                    Map<String,Symbol> temp_map2 = temp_map.get("map2");

                    //locate the refer
                    for (Map.Entry<Integer,Code> entry1 : temp_map1.entrySet()){
                        int addr1 = entry1.getKey(); //get the addr1 for code

                        int n = 0;

                        for (Map.Entry<String,Symbol> entry2 : temp_map2.entrySet()){
                            if (addr1 == entry2.getValue().addr){    //if addr match refer
                                n++;
                                //change the code_addr to symbol_addr
                                String refer = entry2.getKey();
                                if (sym.containsKey(refer)){
                                    int op = sym.get(entry2.getKey()).addr;
                                    entry1.getValue().value = op;
                                }else{
                                    //Error type 2
                                    entry1.getValue().error = "    "+refer+" is not defined; zero used.";
                                }
                                //System.out.println(""+addr2+":"+op);
                                //System.out.println((entry1.getValue().value));
                                //System.out.println(temp_map1.get(addr1).value);
                            }
                        }

                    }

                    //get the addr1-value
                    for (int j=ModuleCounter.get(i)-1;j>=0;j--) {
                        //System.out.println("No:"+j);
                        Code code = temp_map1.get(j);
                        int n = 0;
                        for (Symbol refersym : temp_map2.values()){
                            if(refersym.addr==j){
                                n++;
                            }
                        }
                        //System.out.println(""+code.type+":"+code.value);
                        int code_addr = Integer.valueOf(code.addr);
                        if (temp_map1.containsKey(code_addr)){
                            Code c = new Code();
                            c = temp_map1.get(code_addr);
                            if(code.type.equals("4")){
                                temp_map1.get(code_addr).value = code.value;
                            }
                            if (code.type.equals("1")&& code.value!=0){
                                //System.out.println(" "+code_addr);

                                c.type="4";
                                c.error = "Error: Immediate address on use list; treated as External.";
                                temp_map1.put(code_addr,c);

                            }
                            if (code.type.equals("4")&& code.value==0){
                                c.type="1";
                                c.error = "Error: E type address not on use chain; treated as I type.";
                                temp_map1.put(code_addr,c);
                            }
                        }


                        //System.out.println("n:"+n);

                    }
                    /*

                    for (int j=ModuleCounter.get(i)-1;j>=0;j--) {
                        //System.out.println("No:"+j);
                        Code code = temp_map1.get(j);
                        int addr1 = j; //get the addr1 for code
                        int addr2 = get_addr2(i, j, ModuleCounter); //get the addr2 for code


                        if (code.type.equals("4") ){
                            //System.out.println(addr2);
                            //Output output = temp_map1.get(addr1).root;
                            int code_addr = Integer.valueOf(code.addr);
                            //System.out.println(" "+code_addr);
                            if (temp_map1.containsKey(code_addr)){
                                int op = code.value;
                                //System.out.println("op:"+op);
                                Code c = new Code();
                                c = temp_map1.get(code_addr);
                                temp_map1.get(code_addr).value = op;
                                temp_map1.put(code_addr,c);
                                //System.out.println(""+code_addr+":"+temp_map1.get(code_addr).value);
                            }

                        }
                    }
                    */


                    //Print Memory Map


                    for (Map.Entry<Integer,Code> entry1 : temp_map1.entrySet()) {

                        int addr1 = entry1.getKey(); //get the addr1 for code
                        //System.out.println("No:"+entry1.getKey());
                        Code code = entry1.getValue();
                        int addr2 = get_addr2(i, addr1, ModuleCounter); //get the addr2 for code

                        int op;
                        String addr ="";

                        switch (code.type ){
                            case "1":
                                op = Integer.valueOf(code.opcode)*1000+Integer.valueOf(code.addr);
                                addr = Integer.toString(op);
                                break;
                            case "2":
                                op = Integer.valueOf(code.opcode)*1000+Integer.valueOf(code.addr);
                                addr = Integer.toString(op);
                                break;
                            case "3":
                                op = Integer.valueOf(code.opcode) * 1000;
                                if (code.value != 0){
                                    op += code.value;
                                }else{
                                    op += Integer.valueOf(code.addr);
                                    for(int n=0; n<i;n++){
                                        op += ModuleCounter.get(n);
                                    }
                                }
                                addr = Integer.toString(op);
                                break;
                            case "4":
                                op = Integer.valueOf(code.opcode)*1000+code.value;
                                addr = Integer.toString(op);
                                break;
                        }

                        System.out.print(""+addr2+":"+addr);
                        if(code.error ==null || code.error.isEmpty()){
                            System.out.println();
                        }else{
                            System.out.println(code.error);
                        }
                    }

                }
                //Print Warning.
                for (Map.Entry<String,Symbol> entry1 : sym.entrySet()){
                    int n = 0;
                    for(Map.Entry<Integer,Map> entry0 : Module.entrySet()){
                        Map<String,Map> temp_map = entry0.getValue();
                        Map<String,Symbol> temp_map2 = temp_map.get("map2");
                        for (Map.Entry<String,Symbol> entry2 :temp_map2.entrySet()){
                            if(entry1.getKey().equals(entry2.getKey())){
                                n ++;
                            }
                        }

                    }
                    if (n==0){
                        System.out.println("Warning: "+entry1.getKey()+" was defined in module "+
                                entry1.getValue().module+" but never used.");
                    }
                }



            }else{

            }
            input.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }





    }

}
