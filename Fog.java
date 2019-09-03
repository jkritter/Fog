
/* Fog v1.0
 * by J.Kritter
 * 2019
 * */

import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.security.MessageDigest;

public class Fog {
	
	public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int choice = 0;
        do {
            System.out.println("Press <E> for encoding and <D> for decoding");
            String s = sc.nextLine().toLowerCase();
            
            if (s.equals("e")) {
                choice = 1;
            } else if (s.equals("d")) {
                choice = 2;
            }
            
        } while (choice == 0);
        
        File folder = new File("./");
        File[] listFiles = folder.listFiles();
        ArrayList<File> ListImages = new ArrayList<File>();
        
        for(File f : listFiles) { // Recherche toutes les images du document
            String format = f.getName().substring(f.getName().indexOf(".")).toLowerCase();
            String name = f.getName().substring(0, f.getName().indexOf("."));
            if (format.equals(".png") || format.equals(".jpg") || format.equals(".bmp")) {
                if (name.length() >= 4) {
                    if (!name.substring(0,4).equals("FOG_") && choice == 1) {
                        ListImages.add(f);
                    } else if (name.substring(0,4).equals("FOG_") && choice == 2) {
                        ListImages.add(f);
                    }
                } else {
                    if (choice == 1) {
                        ListImages.add(f);
                    }
                }
            }
        }
        
        System.out.println("Enter key : ");
        String raw_key = sc.nextLine();
        System.out.println("...");
        
        ArrayList<Integer> Key = new ArrayList<Integer>();
        
        try { // Hash la clé
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(raw_key.getBytes());
            String hash_key = new String(messageDigest.digest());
            
            for (int i = 0; i < hash_key.length(); i++) {
                char ch = hash_key.charAt(i);
                int pos = ch;
                Key.add((pos));
            }
            
        } catch (Exception e) {System.out.println("Exception occured :" + e.getMessage());}
        
        for (int i=0; i<ListImages.size(); i++) {
            if (choice == 1) {
                    Encoder(Key, ListImages.get(i));
                    System.out.println(ListImages.get(i).getName() + " Encoding completed (" + (i+1) + "/" + ListImages.size() + ")");
                    ListImages.get(i).delete();
            } else if (choice == 2) {
                    Decoder(Key, ListImages.get(i));
                    System.out.println(ListImages.get(i).getName() + " Decoding completed (" + (i+1) + "/" + ListImages.size() + ")");
                    ListImages.get(i).delete();
            }
        }
        
        System.exit(0);
	}
    
    public static void Encoder(ArrayList<Integer> Key, File f) {
        
        BufferedImage image;
        BufferedImage image_result;
        
        try {
            
            image = ImageIO.read(f);
            image_result = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_RGB);
            
            String name = f.getName();
            String nameBmp = "FOG_" + name.substring(0, name.indexOf(".")) + ".bmp";
            
            ImageIO.write(image, "bmp", new File(nameBmp));
            image = ImageIO.read(new File(nameBmp));
            
            int n = 0;
            int p = 0;
            int key_size = Key.size();
            
            ArrayList<Integer> StableKey = new ArrayList<Integer>();
            for (int num : Key) {
                StableKey.add(num);
            }
            
            ArrayList<Integer> ListWidth = rangeList(image.getWidth());
            Collections.shuffle(ListWidth, new Random(StableKey.get(0)));
            
            for (int i = 0; i < image.getWidth(); i++) {
                
                ArrayList<Integer> ListHeight = rangeList(image.getHeight());
                Collections.shuffle(ListHeight, new Random(StableKey.get(i % key_size)));
                
                for (int j = 0; j < image.getHeight(); j++) {
                    Color c = new Color(image.getRGB(i, j));
                    
                    int red = c.getRed() + Key.get(p % key_size) + n;
                    p++;
                    int green = c.getGreen() + Key.get(p % key_size) + n;
                    p++;
                    int blue = c.getBlue() + Key.get(p % key_size) + n;

                    image_result.setRGB(ListWidth.get(i), ListHeight.get(j), new Color(red % 256, green % 256, blue % 256).getRGB());
                    
                    n += Key.get(p % key_size);
                    Key.set(p % key_size, n % 256);
                    p++;
                }

            }
            
            ImageIO.write(image_result, "bmp", new File(nameBmp));
            
        } catch (Exception e) {System.out.println("Exception occured :" + e.getMessage());}
    }
    
    public static void Decoder(ArrayList<Integer> Key, File f) {
        
        BufferedImage image2;
        BufferedImage image2_result;
        
        try {
            
            image2 = ImageIO.read(f);
            image2_result = new BufferedImage(image2.getWidth(),image2.getHeight(),BufferedImage.TYPE_INT_RGB);
            String name = f.getName();
            
            int n = 0;
            int p = 0;
            int key_size = Key.size();
            
            ArrayList<Integer> StableKey = new ArrayList<Integer>();
            for (int num : Key) {
                StableKey.add(num);
            }
            
            ArrayList<Integer> ListWidth = rangeList(image2.getWidth());
            Collections.shuffle(ListWidth, new Random(StableKey.get(0)));
            
            for (int i = 0; i < image2.getWidth(); i++) {
                
                ArrayList<Integer> ListHeight = rangeList(image2.getHeight());
                Collections.shuffle(ListHeight, new Random(StableKey.get(i % key_size)));
                
                for (int j = 0; j < image2.getHeight(); j++) {
                    Color c = new Color(image2.getRGB(ListWidth.get(i), ListHeight.get(j)));
                    
                    int red = c.getRed() - Key.get(p % key_size) - n;
                    p++;
                    red = seuil0(red);
                    int green = c.getGreen() - Key.get(p % key_size) - n;
                    p++;
                    green = seuil0(green);
                    int blue = c.getBlue() - Key.get(p % key_size) - n;
                    blue = seuil0(blue);
                    
                    image2_result.setRGB(i, j, new Color(red, green, blue).getRGB());
                    
                    n += Key.get(p % key_size);
                    Key.set(p % key_size, n % 256);
                    p++;

                }
            }
            
            ImageIO.write(image2_result, "jpg", new File(name.substring(4, name.indexOf(".")) + ".jpg"));
            
        } catch (Exception e) {System.out.println("Exception occured :" + e.getMessage());}
        
    }
    
    public static int seuil0(int n) {
        while (n < 0) {
            n += 256;
        }
        return n;
    }
    
    public static ArrayList<Integer> rangeList(int n) {
        ArrayList<Integer> List = new ArrayList<Integer>();
        for (int i=0; i<n; i++) {
            List.add(i);
        }
        return List;
    }
}
