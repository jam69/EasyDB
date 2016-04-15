/*
 *  Copyright &copy; Indra 2016
 */
package com.jam69.easydb;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamartinm
 */
public class Main
{
    private final static Logger log=LoggerFactory.getLogger(Main.class);
    public static void main(String[] args){
        System.out.println("Hola");
        Main m=new Main();
        FileReader reader;
        try
        {
            reader = new FileReader(args[1]);
            m.prueba(reader);
        } catch (FileNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }

    private void prueba(FileReader file){
        PersistenceBase p=new PersistenceBase();
        p.setDBOptionsFile(file);

        //DateFormat df=new SimpleDateFormat("yyyymmdd");
        String  d1="20160301";
        String  d2="20160331";

        //p.callProcedure("OBTENER_KM_SERVICIOS4", "TI",d1,d2,-1,-1);
       // p.callProcedure("TESTPROC", "WWW");

        p.newSelect("ID_USUARIO,FECHA_INICIO,FECHA_FIN,COD_EMPRESA,COD_LINEA,"
            + "COD_ITINERARIO,EXPEDICION,TURNOS,CONDUCTORES,KILOMETROS,ID_EXPEDICION_PLANIF")
            .from("VW_KM_SERVICIOS")
            .dump();

//         p.newSelect("*")
//            .from("CH_HELP")
//            .dump(new PrintWriter(System.out));
    }
}
