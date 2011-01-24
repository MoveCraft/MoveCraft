package com.bukkit.yogoda.movecraft;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
//import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.block.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Joel
 */
public class CraftBuilder {

    private static Craft craft;

    //used for the detection of blocks
    private static Stack<BlockLoc> blocksStack;
    private static HashMap<BlockLoc, BlockLoc> blocksList = null;

    private static HashMap<Integer,HashMap<Integer,HashMap<Integer,Short>>> dmatrix;

    private static Short nullBlock = -1;
    private static boolean isFree(int x, int y, int z){

        if(x < 0 || x >= craft.sizeX ||
           y < 0 || y >= craft.sizeY ||
           z < 0 || z >= craft.sizeZ)
            return true;

        int blockId = craft.matrix[x][y][z];

        if(blockId == 0 || blockId == -1)
            return true;
        
        return false;
    }

    private static Short get(int x, int y, int z){

       HashMap<Integer,HashMap<Integer,Short>> xRow;
       HashMap<Integer,Short> yRow;

       xRow = dmatrix.get(new Integer(x));
       if(xRow!=null){

           yRow = xRow.get(new Integer(y));

           if(yRow!=null){
               return yRow.get(new Integer(z));
           }
       }

       return null;
    }

    private static void set(short blockType, int x, int y, int z){

       HashMap<Integer,HashMap<Integer,Short>> xRow;

       //create an x row if it does not exists
       xRow = dmatrix.get(new Integer(x));
       if(xRow==null){
           xRow = new HashMap<Integer,HashMap<Integer,Short>>();
           dmatrix.put(new Integer(x), xRow);
       }

       HashMap<Integer,Short> yRow;

       //get the y row, create it if it does not exists
       yRow = xRow.get(new Integer(y));

       if(yRow==null){
           yRow = new HashMap<Integer,Short>();
           xRow.put(new Integer(y), yRow);
       }

       Short type = yRow.get(new Integer(z));

       if(type==null){
           yRow.put(new Integer(z), new Short(blockType));
       }

    }

   private static void detectWater(World world, int x, int y, int z){

       //craft block
       if(x >= 0 && x < craft.sizeX && y >= 0 && y < craft.sizeY && z >= 0 && z < craft.sizeZ &&
          craft.matrix[x][y][z] != -1) return;

       //int blockId = etc.getServer().getBlockIdAt(craft.posX + x, craft.posY + y, craft.posZ + z);
       Block theBlock = world.getBlockAt(craft.posX + x, craft.posY + y, craft.posZ + z);
       int blockId = theBlock.getTypeId();

       //found water, record water level and water type
       if(blockId == 8 || blockId == 9){ //water
           if(y > craft.waterLevel) craft.waterLevel = y;
           craft.waterType = 8;
           return;
       }

       //found lava, record lava level and water type
       if(blockId == 10 || blockId == 11){ //lava
           if(y > craft.waterLevel) craft.waterLevel = y;
           craft.waterType = 10;
           return;
       }
   }

    //remove water blocks that have been incorrectly added
   private static void removeWater(){

       boolean updated;

       do{

           updated = false;

           for(int x=0;x<craft.sizeX;x++){
             for(int z=0;z<craft.sizeZ;z++){
                 for(int y=0;y<craft.sizeY;y++){

                     if(craft.matrix[x][y][z] >= 8 && craft.matrix[x][y][z] <= 11){

                           if(isFree(x + 1, y, z) ||
                           isFree(x - 1, y, z) ||
                           isFree(x, y, z + 1) ||
                           isFree(x, y, z - 1) ||
                           isFree(x, y - 1, z)){

                               craft.matrix[x][y][z] = -1;
                               updated = true;

                               //craft.thePlayer.sendMessage("water removed");
                           }
                     }
                 }
             }
           }
       }while(updated);
   }

    //detect and create an air bubble surrounding the player
   private static boolean createAirBubble(){

       BlockLoc block = blocksStack.pop();

      //location have already been visited
      if(blocksList.get(block) != null)
           return true;

      //mark the block as visited
      blocksList.put(block, block);

      //System.out.println("" + block.x + "" + block.y + "" + block.z);

        if(block.x < 0 || block.x > craft.maxX - craft.posX ||
          block.y < 0 || block.y > craft.maxY - craft.posY ||
          block.z < 0 || block.z > craft.maxZ - craft.posZ){

          return false;

        }

      if(craft.matrix[block.x][block.y][block.z] == -1){

           //empty space touching the bounding box, we have a hole !
           if(block.x == 0 || block.x == craft.maxX - craft.posX ||
              block.y == 0 || block.y == craft.maxY - craft.posY ||
              block.z == 0 || block.z == craft.maxZ - craft.posZ){

              return false;

           }

          //add air
          craft.matrix[block.x][block.y][block.z] = 0;

      } else {

           return true;
      }

      //explore all 6 directions
      blocksStack.push(new BlockLoc(block.x + 1, block.y, block.z));
      blocksStack.push(new BlockLoc(block.x - 1, block.y, block.z));
      blocksStack.push(new BlockLoc(block.x, block.y + 1, block.z));
      blocksStack.push(new BlockLoc(block.x, block.y - 1, block.z));
      blocksStack.push(new BlockLoc(block.x, block.y, block.z + 1));
      blocksStack.push(new BlockLoc(block.x, block.y, block.z - 1));

      return true;

   }

   //second pass detection, we have the craft blocks, now we go from bottom to top,
   //add all missing blocks, detect water level
   private static boolean secondPassDetection(World world){

      //boolean needWaterDetection = false;

      for(int x=0;x<craft.sizeX;x++){
          for(int z=0;z<craft.sizeZ;z++){

               boolean floor = false; //if we have reached the craft floor

               for(int y=0;y<craft.sizeY;y++){

                   //we reached the floor of the craft
                   if(!floor && craft.matrix[x][y][z] != -1){
                        floor = true;
                        continue;
                   } else
                   //free space, check there is no block here
                   if(floor && craft.matrix[x][y][z] == -1){

                       Block block = world.getBlockAt(craft.posX + x, craft.posY + y, craft.posZ + z);
                       int blockId = block.getTypeId();
                       
                        craft.matrix[x][y][z] = (short)blockId; // record this block as part of the craft, also fill with air

                        if(BlocksInfo.isDataBlock(blockId)){
                            addDataBlock(world, craft.posX + x, craft.posY + y, craft.posZ + z);
                        }

                        if(BlocksInfo.isComplexBlock(blockId)){
                        	System.out.println("Complex block found of ID " + blockId);
                        }
                        /*
                        if(BlocksInfo.isComplexBlock(blockId)){
                            addComplexBlock(craft.posX + x, craft.posY + y, craft.posZ + z);
                        }
                        */
                        if(blockId == 63) //sign
                        {
                        	addComplexBlock(world, craft.posX + x, craft.posY + y, craft.posZ + z);
                        }

                       //System.out.println("add " + blockId);

                       /*
                       //omg there was water or lava on this ship, we need to detect the waterlevel/watertype again
                       if(blockId >= 8 && blockId <= 11){
                           needWaterDetection = true;
                       }
                        */

                       //there is a problem with ice that spawn a source block, we can't have ice
                       if(blockId==79){
                           craft.thePlayer.sendMessage("§cSorry, you can't have ice in the " + craft.name);
                           return false;
                       }

                   }
               }
          }
       }

      //water detected, we do the detection of the water level
       if(craft.waterType != 0){

           craft.waterLevel = -1;

           // player.sendMessage("need water detection");
           for(int x=0;x<craft.sizeX;x++){
               for(int z=0;z<craft.sizeZ;z++){
                   for(int y=0;y<craft.sizeY;y++){
                       if(craft.matrix[x][y][z] != -1){
                           detectWater(world, x + 1, y, z);
                           detectWater(world, x - 1, y, z);
                           detectWater(world, x, y, z + 1);
                           detectWater(world, x, y, z - 1);
                       }
                   }
               }
           }

          //remove water blocks that can flow out of the craft
          removeWater();
       }

      //there is water detected
     if(craft.waterLevel != -1){

         //remove air above the water level (so the part under water have still air)
         for(int x=0;x<craft.sizeX;x++){
             for(int z=0;z<craft.sizeZ;z++){
                  for(int y=craft.waterLevel + 1;y<craft.sizeY;y++){
                      if(craft.matrix[x][y][z]==0)
                          craft.matrix[x][y][z] = -1;
                  }
             }
         }
      //no water, remove ALL air
      } else {
         for(int x=0;x<craft.sizeX;x++){
             for(int z=0;z<craft.sizeZ;z++){
                  for(int y=0;y<craft.sizeY;y++){
                      if(craft.matrix[x][y][z]==0)
                          craft.matrix[x][y][z] = -1;
                  }
             }
         }
      }

      //if the craft can dive, we need to create an air bubble surrounding the player
      //if it touch the bounding box walls, then the submarine has a hole !
      if(craft.type.canDive){

         //to store explored blocks
         blocksList = new HashMap<BlockLoc, BlockLoc>();
         blocksStack = new Stack<BlockLoc>();

         //start with the player's head
         blocksStack.push(new BlockLoc((int)Math.floor(craft.thePlayer.getLocation().getX()) - craft.posX,
                                          (int)Math.floor(craft.thePlayer.getLocation().getY() + 1 - craft.posY),
                                          (int)Math.floor(craft.thePlayer.getLocation().getZ()) - craft.posZ));

         //detect all connected empty blocks
           do{
               if(!createAirBubble()){

                   craft.thePlayer.sendMessage("§eThis " + craft.type.name + " have holes, it needs to be waterproof");
                   return false;
               }
           }
           while(!blocksStack.isEmpty());

           blocksStack = null;
           blocksList = null;

      }

      return true;

   }

   private static void addDataBlock(World world, int x, int y, int z){
        craft.dataBlocks.add(new Craft.DataBlock(x - craft.posX,y - craft.posY,z - craft.posZ, world.getBlockAt(x, y, z).getData()));
   }

   private static void addComplexBlock(World world, int x, int y, int z){

	   craft.complexBlocks.add(world.getBlockAt(x - craft.posX,
                                                            y - craft.posY,
                                                            z - craft.posZ));
	   /*
        craft.complexBlocks.add(new Craft.CraftComplexBlock(x - craft.posX,
                                                            y - craft.posY,
                                                            z - craft.posZ,
                                                            null));
                                                            */
   }
   
   private static void addEngineBlock(Block block)
   {
	   craft.engineBlocks.add(block);
   }

    //put all data in a standard matrix to be more efficient
    private static void createMatrix(World world){

      craft.matrix = new short[craft.sizeX][craft.sizeY][craft.sizeZ];
      craft.dataBlocks = new ArrayList();
      craft.complexBlocks = new ArrayList();

      for(int x=0;x<craft.sizeX;x++){
          for(int z=0;z<craft.sizeZ;z++){
              for(int y=0;y<craft.sizeY;y++){
                  craft.matrix[x][y][z] = -1;
              }
          }
       }

       for(Integer x:dmatrix.keySet()){
           HashMap<Integer,HashMap<Integer,Short>> xRow = dmatrix.get(x);
           for(Integer y:xRow.keySet()){
               HashMap<Integer,Short> yRow = xRow.get(y);
               for(Integer z:yRow.keySet()){
                   
                   short blockId = yRow.get(z);
                   
                   if(blockId == -1)
                       continue;

                   craft.matrix[x - craft.posX][y - craft.posY][z - craft.posZ] = blockId;

                   if(BlocksInfo.isDataBlock(blockId)){
                        addDataBlock(world, x, y, z);
                   }
                   if(BlocksInfo.isComplexBlock(blockId)){
                        addComplexBlock(world, x, y, z);
                	   //addDataBlock(world, x, y, z);
                   }
                   if(blockId == 61 || blockId == 62)
                	   addEngineBlock(world.getBlockAt(x,y,z));
               }
           }
       }

       dmatrix = null; //release the dynamic matrix now we don't need it anymore
    }

    private static void detectBlock(World world, int x, int y, int z, int dir){

       Short blockType = get(x, y, z);

       //location have already been visited
       if(blockType!=null) return;

       //blockType = new Short((short)etc.getServer().getBlockIdAt(x, y, z));
       //int blockData = etc.getServer().getBlockData(x, y, z);
       blockType = new Short((short) world.getBlockAt(x, y, z).getTypeId());
       //int BlockData = world.getBlockAt(x, y, z).getData();

       //found water, record water level and water type
       if(blockType == 8 || blockType == 9){ //water
           if(y > craft.waterLevel) craft.waterLevel = y;
           craft.waterType = 8;
           set(nullBlock, x, y, z);
           return;
       }

       //found lava, record lava level and water type
       if(blockType == 10 || blockType == 11){ //lava
           if(y > craft.waterLevel) craft.waterLevel = y;
           craft.waterType = 10;
           set(nullBlock, x, y, z);
           return;
       }

       //found air
       if(blockType == 0){ //air
           set(nullBlock, x, y, z);
           return;
        }

       //special blocks
       if(blockType == 55){ //redstone wires
           if(dir!=1){
               set(nullBlock, x, y, z);
               return;
           }
       } else

       //return when the block type is not part of the craft structure
       //default block list
       if(craft.type.structureBlocks == null){

           //only block types supported to make the base of the craft
           if(!(blockType == 4 ||
              blockType == 5 ||
              blockType == 17 ||
              blockType == 19 ||
              blockType == 20 ||
              blockType == 35 ||
              (blockType >= 41 && blockType <= 50) ||
              blockType == 53 ||
              blockType == 55 ||
              blockType == 57 ||
              blockType == 65 ||
              blockType == 67 ||
              blockType == 68 ||
              blockType == 69 ||
              blockType == 75 ||
              blockType == 76 ||
              blockType == 77 ||
              blockType == 85 ||
              blockType == 87 ||
              blockType == 88 ||
              blockType == 89)
              ){
               set(nullBlock, x, y, z);
               return;
              }

        //custom block list defined in the config file
        } else {

           boolean found = false;
           for(short blockId:craft.type.structureBlocks){
                if(blockType == blockId) found = true;
           }
           if(!found){
               set(nullBlock, x, y, z);
               return;
           }
        }

       //record block type at this location
       set(blockType, x, y, z);

       craft.blockCount++;
       if(craft.blockCount > craft.type.maxBlocks){
           return;
       }

       if(blockType == craft.type.flyBlockType){
           craft.flyBlockCount ++;
       }

       if(x < craft.minX) craft.minX = x;
       if(x > craft.maxX) craft.maxX = x;
       if(y < craft.minY) craft.minY = y;
       if(y > craft.maxY) craft.maxY = y;
       if(z < craft.minZ) craft.minZ = z;
       if(z > craft.maxZ) craft.maxZ = z;

       //don't propagate through items that need a support
       if(BlocksInfo.needsSupport(blockType)) return;

       blocksStack.push(new BlockLoc(x, y, z));
    }

    //detect the craft you are in
    private static void detectBlock(World world, BlockLoc block){

       //explore all directions

       //face-face connection
       detectBlock(world, block.x + 1, block.y, block.z, 1);
       detectBlock(world, block.x - 1, block.y, block.z, 2);
       detectBlock(world, block.x, block.y + 1, block.z, 1);
       detectBlock(world, block.x, block.y - 1, block.z, 6);
       detectBlock(world, block.x, block.y, block.z + 1, 3);
       detectBlock(world, block.x, block.y, block.z - 1, 4);

       //edge-edge horizontal connection
       detectBlock(world, block.x + 1, block.y - 1, block.z, -1);
       detectBlock(world, block.x - 1, block.y - 1, block.z, -1);
       detectBlock(world, block.x, block.y - 1, block.z + 1, -1);
       detectBlock(world, block.x, block.y - 1, block.z - 1, -1);
       detectBlock(world, block.x + 1, block.y + 1, block.z, -1);
       detectBlock(world, block.x - 1, block.y + 1, block.z, -1);
       detectBlock(world, block.x, block.y + 1, block.z + 1, -1);
       detectBlock(world, block.x, block.y + 1, block.z - 1, -1);

    }

    public static boolean detect(World world, Craft craft, int X, int Y, int Z){

       CraftBuilder.craft = craft;
        
       dmatrix = new HashMap<Integer,HashMap<Integer,HashMap<Integer,Short>>>();

       craft.blockCount = 0;

       craft.minX = craft.maxX = X;
       craft.minY = craft.maxY = Y;
       craft.minZ = craft.maxZ = Z;

       blocksStack = new Stack<BlockLoc>();
       blocksStack.push(new BlockLoc(X, Y, Z));

       //detect all connected blocks
       do{
            detectBlock(world, blocksStack.pop());
       }
       while(!blocksStack.isEmpty());

       blocksStack = null;

       //max block count have been reached, craft can't be detected !
       if(craft.blockCount > craft.type.maxBlocks){
           craft.thePlayer.sendMessage("§cUnable to detect the " + craft.name + ", be sure it is not connected");
           craft.thePlayer.sendMessage("§c to the ground, or maybe it is too big for this type of craft");
           craft.thePlayer.sendMessage("§cThe maximum size is " + craft.type.maxBlocks + " blocks");
           return false;
       }
       else
       if(craft.blockCount <  craft.type.minBlocks)
       {
           if(craft.blockCount==0){
                craft.thePlayer.sendMessage("§cThere is no " + craft.name + " here");
                craft.thePlayer.sendMessage("§cBe sure you are standing on a block");
           }
           else{
                craft.thePlayer.sendMessage("§cThis " + craft.name + " is too small !");
                craft.thePlayer.sendMessage("§cYou need to add " + (craft.type.minBlocks - craft.blockCount) + " blocks");
           }

           return false;
       }
       //the recursive algorithm returned before the max block count have been reached, we have a craft !
       else{

           //check the craft is not already in controlled by someone
           for(Craft c:Craft.craftList){
               if(c != craft && c.isOnBoard){
                   //check for intersection between 2 cubes
                   if( !((craft.minX < craft.minX && craft.maxX < craft.minX) || (craft.minX < craft.minX && craft.maxX < craft.minX )))
                       if( !((craft.minY < craft.minY && craft.maxY < craft.minY) || (craft.minY < craft.minY && craft.maxY < craft.minY )))
                           if( !((craft.minZ < craft.minZ && craft.maxZ < craft.minZ) || (craft.minZ < craft.minZ && craft.maxZ < craft.minZ ))){
                               craft.thePlayer.sendMessage("§c" + craft.thePlayer.getName() + " is already controling this " + craft.name);
                               return false;
                           }
               }
           }

           //create the craft matrix
           craft.sizeX = (craft.maxX - craft.minX) + 1;
           craft.sizeY = (craft.maxY - craft.minY) + 1;

           //System.out.println(" maxZ : " + maxZ);
           //System.out.println(" minZ : " + minZ);

           craft.sizeZ = (craft.maxZ - craft.minZ) + 1;

           //craft origin and position on the map
           craft.posX = craft.minX;
           craft.posY = craft.minY;
           craft.posZ = craft.minZ;

           if(craft.waterLevel != -1)
               craft.waterLevel = craft.waterLevel - craft.posY;

           /*
           //player offset from the craft origin
           offX = (float)(player.getX() - posX);
           offY = (float)(player.getY() - posY);
           offZ = (float)(player.getZ() - posZ);
           */

           createMatrix(world);

           if(!secondPassDetection(world)) //second pass, add some blocks, check for water problems
                return false;

           //the ship is not on water
           //if(craft.type.canNavigate && !craft.type.canFly && craft.waterType == 0){
           if(craft.type.canNavigate && !craft.type.canFly && craft.waterType == 0 && !craft.type.canDig){
               craft.thePlayer.sendMessage("§cThis " + craft.name + " is not on water...");
               return false;
           } else
           //the submarine is not into water
           //if(craft.type.canDive && !craft.type.canFly && craft.waterType == 0){
           if(craft.type.canDive && !craft.type.canFly && craft.waterType == 0 && !craft.type.canDig){
               craft.thePlayer.sendMessage("§cThis " + craft.name + " is not into water...");
               return false;
           } else
           //the airplane / airship is into water
           if(craft.type.canFly && !craft.type.canNavigate && !craft.type.canDive && craft.waterLevel > -1){
               craft.thePlayer.sendMessage("§cThis " + craft.name + " is into water...");
               return false;
           }

           //an airship needs to have x percent of flystone to be able to move
           if(craft.type.canFly && craft.type.flyBlockType != 0){

               //int flyBlocksNeeded = (int)Math.floor((blockCount - flyBlockCount) * ((float)type.flyBlockPercent * 0.01));

               //let's hope it is correct :P
               int flyBlocksNeeded = (int)Math.floor((craft.blockCount - craft.flyBlockCount) * ((float)craft.type.flyBlockPercent * 0.01) / (1 - ((float)craft.type.flyBlockPercent * 0.01)));

               if(flyBlocksNeeded < 1)
                   flyBlocksNeeded = 1;

               if(craft.flyBlockCount < flyBlocksNeeded){
                   craft.thePlayer.sendMessage("§cNot enough " + craft.type.flyBlockName + " to make this " + craft.name + " move");
                   craft.thePlayer.sendMessage("§cYou need to add " + (flyBlocksNeeded - craft.flyBlockCount) + " more" );
                   return false;
               }
           }
           
           //drill needs to have a diamond block
           if(craft.type.canDig && craft.type.flyBlockType != 0){
        	   System.out.println("Drill flyblock is " + Integer.toString(craft.type.flyBlockType));

               int flyBlocksNeeded = (int)Math.floor((craft.blockCount - craft.flyBlockCount) * ((float)craft.type.flyBlockPercent * 0.01) / (1 - ((float)craft.type.flyBlockPercent * 0.01)));
               if(flyBlocksNeeded < 1)
                   flyBlocksNeeded = 1;

               if(craft.flyBlockCount < flyBlocksNeeded){
                   craft.thePlayer.sendMessage("§cNot enough " + craft.type.flyBlockName + " to make this " + craft.name + " move");
                   craft.thePlayer.sendMessage("§cYou need to add " + (flyBlocksNeeded - craft.flyBlockCount) + " more" );
                   return false;
               }
           }

           if(craft.customName == null)
                craft.thePlayer.sendMessage("§e" + craft.type.sayOnControl);
           else
                craft.thePlayer.sendMessage("§eWelcome on the §a" + craft.customName + "§e !");
       }

       return true;

    }

}