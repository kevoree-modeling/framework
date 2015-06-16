package org.kevoree.modeling.format.json;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap;
import org.kevoree.modeling.memory.struct.map.KStringMapCallBack;

import java.util.ArrayList;

/**
 * @native ts
 * private readObject:any;
 * public parseObject(payload:string):void {
 * this.readObject = JSON.parse(payload);
 * }
 * public get(name:string):any {
 * return this.readObject[name];
 * }
 * public getAsStringArray(name:string):string[] {
 * return <string[]> this.readObject[name];
 * }
 * public keys():string[] {
 * var keysArr = []
 * for (var key in this.readObject) {
 * keysArr.push(key);
 * }
 * return keysArr;
 * }
 */
public class JsonObjectReader {

    private ArrayStringMap<Object> content = new ArrayStringMap<Object>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);

    private String[] keys = null;

    public void parseObject(String payload) {
        Lexer lexer = new Lexer(payload);
        String currentAttributeName = null;
        ArrayList<String> arrayPayload = null;
        JsonType currentToken = lexer.nextToken();
        while (currentToken != JsonType.EOF) {
            if (currentToken.equals(JsonType.LEFT_BRACKET)) {
                arrayPayload = new ArrayList<String>();
            } else if (currentToken.equals(JsonType.RIGHT_BRACKET)) {
                content.put(currentAttributeName, arrayPayload);
                arrayPayload = null;
                currentAttributeName = null;
            } else if (currentToken.equals(JsonType.VALUE)) {
                if (currentAttributeName == null) {
                    currentAttributeName = lexer.lastValue();
                } else {
                    if (arrayPayload == null) {
                        content.put(currentAttributeName, lexer.lastValue());
                        currentAttributeName = null;
                    } else {
                        arrayPayload.add(lexer.lastValue());
                    }
                }
            }
            currentToken = lexer.nextToken();
        }
    }

    public Object get(String name) {
        return content.get(name);
    }

    public String[] getAsStringArray(String name) {
        Object result = content.get(name);
        if (result instanceof ArrayList) {
            ArrayList<String> casted = (ArrayList<String>) result;
            return casted.toArray(new String[casted.size()]);
        }
        return null;
    }

    public String[] keys() {
        if (keys == null) {
            keys = new String[content.size()];
            int[] nbLoop = new int[1];
            nbLoop[0] = 0;
            content.each(new KStringMapCallBack<Object>() {
                @Override
                public void on(String key, Object value) {
                    keys[nbLoop[0]] = key;
                    nbLoop[0]++;
                }
            });
        }
        return keys;
    }

}
