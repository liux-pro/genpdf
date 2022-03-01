package pdf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Gen {
    public static void main(String[] args) throws IOException {
        File file = new File(args[0]);
        String name = file.getName();
        if (!Pattern.matches(".+\\.abs\\.json", name)){
            System.out.println("request file *.abs.json");
            System.exit(-1);
        }

        FileInputStream fileInputStream = new FileInputStream(args[0]);
        byte[] bytes = fileInputStream.readAllBytes();
        String jsonString = new String(bytes, StandardCharsets.UTF_8);
        LinkedHashMap<String, ArrayList<String>> linkedHashMap = JSON.parseObject(jsonString, new TypeReference<>() {
        });
        ArrayList<String> all = new ArrayList<>();
        HashMap<String, String> menuMap = new HashMap<>();
        for (Map.Entry<String, ArrayList<String>> entry : linkedHashMap.entrySet()) {
            String key = entry.getKey();
            key=key.replaceFirst(".*?@.*?@","");
            ArrayList<String> list = entry.getValue();
            all.addAll(list);
            menuMap.put(list.get(0), key);
        }


        String pdfName = name.replace(".abs.json", "")+".pdf";
        CombineIntoPDF.combineImagesIntoPDF(pdfName,menuMap, all.toArray(new String[0]));
    }
}
