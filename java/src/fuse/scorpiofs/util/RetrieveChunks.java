package fuse.scorpiofs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fuse.scorpiofs.ScorpioFS;

import unipi.p2p.chord.ObjectDiskIO;
import unipi.p2p.chord.RemoteChordNode;
import fuse.scorpiofs.util.Constants;

public class RetrieveChunks{
	private static final Log log = LogFactory.getLog(RetrieveChunks.class);

	public File koko(RemoteChordNode localChordNode,File interFileName){
		ObjectDiskIO objectReader=new ObjectDiskIO();
		byte[] buffer=new byte[1048576];
		File newFsTree=null;
		
		try{
			
			log.info("interFile exists: "+interFileName.exists() + interFileName.length());			
			FsTreeChunks ftc=(FsTreeChunks)objectReader.loadObject(interFileName);
			Iterator<BigInteger> it=ftc.getIDs();
			FileOutputStream fos=new FileOutputStream(Constants.fsTreeName+".enc");
			while(it.hasNext()){
				BigInteger idTemp=it.next();
				log.info("Chunk id: "+idTemp);
				//Here revoke chunks from network
				buffer=ChunkNetwork.getChunks(idTemp, localChordNode);
				try{
					fos.write(buffer);
					fos.flush();
				}catch(IOException e0){
					e0.printStackTrace();
				}
			}
			fos.close();

			FileInputStream fis=new FileInputStream(Constants.fsTreeName+".enc");
			fos=new FileOutputStream(Constants.fsTreeName);
			FileCrypto fc=new FileCrypto();
			fc.decrypt(fis, fos);
			fis.close();
			fos.close();
			//finally deserialize fstree
			newFsTree=new File(Constants.fsTreeName);
		}catch(Exception e1){
			e1.printStackTrace();
		}
		
		return newFsTree;
	}
	public void dummy(){
		System.out.println("Just been called");
	}
}
