function javafile(path){
    var p = String(path).replace(/[\/\\]/g,String(java.io.File.separator));
    return new java.io.File(p);
}

function log(m,s){
    process.stdout.writeln("module."+m+": "+s);
}

var Stats = (function(){
    function Stats(path){
        this.javafile=javafile(path);
    }
    Stats.prototype.isFile=function(){
        return Boolean(this.javafile.isFile());
    };
    Stats.prototype.isDirectory=function(){
        return Boolean(this.javafile.isDirectory());
    };
    Stats.prototype.isBlockDevice=function(){
        return false;
    };
    Stats.prototype.isCharacterDevice=function(){
        return false;
    };
    Stats.prototype.isSymbolicLink=function(){
        return false;
    };
    Stats.prototype.isFIFO=function(){
        return false;
    };
    Stats.prototype.isSocket=function(){
        return false;
    };
    return Stats;
})();

var process = {};
(function () {
    function print(writer, o) {
        writer.print(o || "");
    }

    function println(writer, o) {
        writer.println(o || "");
    }

    function platform() {
        var prop = java.lang.System.getProperty;
        return prop("os.name") + " - " + prop("os.version") + " (" + prop("os.version") + ")";
    }

    function exit(status) {
        status = status || 0;
        //throw new com.ppedregal.typescript.maven.ProcessExit(status);
    }

    process = {
        stdout: {
            write: function (o) {
                print(java.lang.System.out, o);
            },
            writeln: function (o) {
                println(java.lang.System.out, o);
            },
            on: function (event, callback) {
                // Graciously ignoring event
            }
        },
        stderr: {
            write: function (o) {
                print(java.lang.System.err, o);
            },
            writeln: function (o) {
                println(java.lang.System.err, o);
            },
            on: function (event, callback) {
                // Graciously ignoring event
            }
        },
        platform: platform(),
        argv: [],
        exit: exit,
        mainModule: {
            filename: ""
        }
    };
})();
var console = {};
var module = {
    exports: function () {
        return true;
    }
};
(function () {
    function doLog() {
        return Array.prototype.slice.call(arguments).join(",");
    }

    function logMsg(lvl, msg) {
        return "[" + lvl + "] " + Array.prototype.slice.call(msg);
    }

    console = {
        log: function () {
            process.stdout.writeln(Array.prototype.slice.call(arguments).join(","));
        },
        info: function () {
            this.log(logMsg("info", arguments));
        },
        warn: function () {
            this.log(logMsg("warn", arguments));
        },
        error: function () {
            this.log(logMsg("error", arguments));
        },
        debug: function () {
            this.log(logMsg("debug", arguments));
        },
        trace: function () {
            this.log(logMsg("trace", arguments));
        }
    };
})();

var setTimeout,
    clearTimeout,
    setInterval,
    clearInterval;

(function () {
    var timer = new java.util.Timer();
    var counter = 1;
    var ids = {};
    setTimeout = function (fn, delay) {
        /* this should work with a newer version of Rhino, but for now it's commented out
         cfr. https://github.com/mozilla/rhino/commit/69b177c7214e0d1ac9656dec33e13aedfe6938a0

         var id = counter++;
         ids[id] = new JavaAdapter(java.util.TimerTask,{run: fn});
         timer.schedule(ids[id],delay);
         return id;
         */
    };
    clearTimeout = function (id) {
        ids[id].cancel();
        timer.purge();
        delete ids[id];
    };
    setInterval = function (fn, delay) {
        var id = counter++;
        ids[id] = new JavaAdapter(java.util.TimerTask, {run: fn});
        timer.schedule(ids[id], delay, delay);
        return id;
    };
    clearInterval = clearTimeout;
})();

function Buffer(data, encoding) {
    this.data = data
    this.encoding = encoding
}

function require(name) {
    if (name == "os") {
        return {
            EOL: (function () {
                return java.lang.System.getProperty("line.separator");
            })(),
            platform: function () {
                return "linux";
            }
        }
    } else if (name == "fs") {
        return {
            readFileSync: function (path, enc) {
                enc = enc || process.encoding || "utf-8";
                var f = javafile(path),
                    reader = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(f), enc));
                try {
                    var buffer = new java.lang.StringBuffer(),
                        line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                        buffer.append("\r\n");
                    }
                } catch (e) {
                    return null;
                } finally {
                    reader.close();
                }
                reader = null;
                return {
                    "0": 0,
                    "1": 0,
                    toString: function () {
                        return new String(buffer.toString());
                    }
                };
            },
            writeFileSync: function (path, data, enc) {
                enc = enc || process.encoding || "utf-8";
                var f = javafile(path),
                    writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(f), enc));
                try {
                    writer.write(data, 0, data.length);
                } catch (e) {
                } finally {
                    writer.close();
                }
                writer = null;
            },
            unlinkSync: function (path) {
                return javafile(path)["delete"]();
            },
            existsSync: function (path) {
                return javafile(path).exists();
            },
            statSync: function (path) {
                return new Stats(path);
            },
            lstatSync: function (path) {
                return new Stats(path);
            },
            fstatSync: function (path) {
                return new Stats(path);
            },
            mkdirSync: function (path) {
                return javafile(path).mkdir();
            },
            openSync: function (path, flags, mode) {
                var enc = process.encoding || "utf-8",
                    f = javafile(path),
                    writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(f), enc));
                return writer;
            },
            writeSync: function (fd, buffer, offset, len, pos) {

            },
            closeSync: function (fd) {
                fd.close();
            },
            readdirSync: function (path) {
                var arr = javafile(path).list();
                for (var i = 0, li = arr.length; i < li; i++) {
                    arr[i] = new String(arr[i]);
                }
                return arr;
            },
            unwatchFile: function () {
            },
            watchFile: function () {
            },
            realpathSync: function () {
                return new String(javafile(path).getCanonicalPath()).replace(/[\/\\]/g, java.io.File.separator);
            }
        }
    } else if (name == "path") {
        return {}
    } else {
        java.lang.System.out.println("Not managed " + name);
    }
}