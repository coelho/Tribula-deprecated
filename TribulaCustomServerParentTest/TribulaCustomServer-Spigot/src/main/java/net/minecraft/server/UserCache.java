package net.minecraft.server;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.commons.io.IOUtils;

public class UserCache {

    public static final SimpleDateFormat a = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private static boolean c;
    private final Map<String, UserCache.UserCacheEntry> d = Maps.newHashMap();
    private final Map<UUID, UserCache.UserCacheEntry> e = Maps.newHashMap();
    private final Deque<GameProfile> f = new java.util.concurrent.LinkedBlockingDeque<GameProfile>(); // CraftBukkit
    private final GameProfileRepository g;
    protected final Gson b;
    private final File h;
    private static final ParameterizedType i = new ParameterizedType() {
        public Type[] getActualTypeArguments() {
            return new Type[] { UserCache.UserCacheEntry.class};
        }

        public Type getRawType() {
            return List.class;
        }

        public Type getOwnerType() {
            return null;
        }
    };

    public UserCache(GameProfileRepository gameprofilerepository, File file) {
        this.g = gameprofilerepository;
        this.h = file;
        GsonBuilder gsonbuilder = new GsonBuilder();

        gsonbuilder.registerTypeHierarchyAdapter(UserCache.UserCacheEntry.class, new UserCache.BanEntrySerializer(null));
        this.b = gsonbuilder.create();
        this.b();
    }

    private static GameProfile a(GameProfileRepository gameprofilerepository, String s) {
        final GameProfile[] agameprofile = new GameProfile[1];
        ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
            public void onProfileLookupSucceeded(GameProfile gameprofile) {
                agameprofile[0] = gameprofile;
            }

            public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                agameprofile[0] = null;
            }
        };

        gameprofilerepository.findProfilesByNames(new String[] { s}, Agent.MINECRAFT, profilelookupcallback);
        if (!d() && agameprofile[0] == null) {
            UUID uuid = EntityHuman.a(new GameProfile(null, s));
            GameProfile gameprofile = new GameProfile(uuid, s);

            profilelookupcallback.onProfileLookupSucceeded(gameprofile);
        }

        return agameprofile[0];
    }

    public static void a(boolean flag) {
        UserCache.c = flag;
    }

    private static boolean d() {
        return UserCache.c;
    }

    public void a(GameProfile gameprofile) {
        this.a(gameprofile, null);
    }

    private void a(GameProfile gameprofile, Date date) {
        UUID uuid = gameprofile.getId();

        if (date == null) {
            Calendar calendar = Calendar.getInstance();

            calendar.setTime(new Date());
            calendar.add(2, 1);
            date = calendar.getTime();
        }

        String s = gameprofile.getName().toLowerCase(Locale.ROOT);
        UserCache.UserCacheEntry usercache_usercacheentry = new UserCache.UserCacheEntry(gameprofile, date, null);

        if (this.e.containsKey(uuid)) {
            UserCache.UserCacheEntry usercache_usercacheentry1 = this.e.get(uuid);

            this.d.remove(usercache_usercacheentry1.a().getName().toLowerCase(Locale.ROOT));
            this.f.remove(gameprofile);
        }

        this.d.put(gameprofile.getName().toLowerCase(Locale.ROOT), usercache_usercacheentry);
        this.e.put(uuid, usercache_usercacheentry);
        this.f.addFirst(gameprofile);
        if( !org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly ) this.c(); // Spigot - skip saving if disabled
    }

    @Nullable
    public GameProfile getProfile(String s) {
        String s1 = s.toLowerCase(Locale.ROOT);
        UserCache.UserCacheEntry usercache_usercacheentry = this.d.get(s1);

        if (usercache_usercacheentry != null && (new Date()).getTime() >= usercache_usercacheentry.c.getTime()) {
            this.e.remove(usercache_usercacheentry.a().getId());
            this.d.remove(usercache_usercacheentry.a().getName().toLowerCase(Locale.ROOT));
            this.f.remove(usercache_usercacheentry.a());
            usercache_usercacheentry = null;
        }

        GameProfile gameprofile;

        if (usercache_usercacheentry != null) {
            gameprofile = usercache_usercacheentry.a();
            this.f.remove(gameprofile);
            this.f.addFirst(gameprofile);
        } else {
            gameprofile = a(this.g, s); // Spigot - use correct case for offline players
            if (gameprofile != null) {
                this.a(gameprofile);
                usercache_usercacheentry = this.d.get(s1);
            }
        }

        if( !org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly ) this.c(); // Spigot - skip saving if disabled
        return usercache_usercacheentry == null ? null : usercache_usercacheentry.a();
    }

    public String[] a() {
        ArrayList arraylist = Lists.newArrayList(this.d.keySet());

        return (String[]) arraylist.toArray(new String[arraylist.size()]);
    }

    @Nullable
    public GameProfile a(UUID uuid) {
        UserCache.UserCacheEntry usercache_usercacheentry = this.e.get(uuid);

        return usercache_usercacheentry == null ? null : usercache_usercacheentry.a();
    }

    private UserCache.UserCacheEntry b(UUID uuid) {
        UserCache.UserCacheEntry usercache_usercacheentry = this.e.get(uuid);

        if (usercache_usercacheentry != null) {
            GameProfile gameprofile = usercache_usercacheentry.a();

            this.f.remove(gameprofile);
            this.f.addFirst(gameprofile);
        }

        return usercache_usercacheentry;
    }

    public void b() {
        BufferedReader bufferedreader = null;

        try {
            bufferedreader = Files.newReader(this.h, Charsets.UTF_8);
            List list = this.b.fromJson(bufferedreader, UserCache.i);

            this.d.clear();
            this.e.clear();
            this.f.clear();
            if (list != null) {
                Iterator iterator = Lists.reverse(list).iterator();

                while (iterator.hasNext()) {
                    UserCache.UserCacheEntry usercache_usercacheentry = (UserCache.UserCacheEntry) iterator.next();

                    if (usercache_usercacheentry != null) {
                        this.a(usercache_usercacheentry.a(), usercache_usercacheentry.b());
                    }
                }
            }
        } catch (FileNotFoundException filenotfoundexception) {
            // Spigot Start
        } catch (com.google.gson.JsonSyntaxException ex) {
            JsonList.a.warn( "Usercache.json is corrupted or has bad formatting. Deleting it to prevent further issues." );
            this.h.delete();
        // Spigot End
        } catch (JsonParseException jsonparseexception) {
        } finally {
            IOUtils.closeQuietly(bufferedreader);
        }

    }

    public void c() {
        String s = this.b.toJson(this.a(org.spigotmc.SpigotConfig.userCacheCap));
        BufferedWriter bufferedwriter = null;

        try {
            bufferedwriter = Files.newWriter(this.h, Charsets.UTF_8);
            bufferedwriter.write(s);
            return;
        } catch (FileNotFoundException filenotfoundexception) {
            return;
        } catch (IOException ioexception) {
        } finally {
            IOUtils.closeQuietly(bufferedwriter);
        }

    }

    private List<UserCache.UserCacheEntry> a(int i) {
        ArrayList arraylist = Lists.newArrayList();
        ArrayList arraylist1 = Lists.newArrayList(Iterators.limit(this.f.iterator(), i));
        Iterator iterator = arraylist1.iterator();

        while (iterator.hasNext()) {
            GameProfile gameprofile = (GameProfile) iterator.next();
            UserCache.UserCacheEntry usercache_usercacheentry = this.b(gameprofile.getId());

            if (usercache_usercacheentry != null) {
                arraylist.add(usercache_usercacheentry);
            }
        }

        return arraylist;
    }

    class UserCacheEntry {

        private final GameProfile b;
        private final Date c;

        private UserCacheEntry(GameProfile gameprofile, Date date) {
            this.b = gameprofile;
            this.c = date;
        }

        public GameProfile a() {
            return this.b;
        }

        public Date b() {
            return this.c;
        }

        UserCacheEntry(GameProfile gameprofile, Date date, Object object) {
            this(gameprofile, date);
        }
    }

    class BanEntrySerializer implements JsonDeserializer<UserCache.UserCacheEntry>, JsonSerializer<UserCache.UserCacheEntry> {

        private BanEntrySerializer() {}

        public JsonElement a(UserCache.UserCacheEntry usercache_usercacheentry, Type type, JsonSerializationContext jsonserializationcontext) {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("name", usercache_usercacheentry.a().getName());
            UUID uuid = usercache_usercacheentry.a().getId();

            jsonobject.addProperty("uuid", uuid == null ? "" : uuid.toString());
            jsonobject.addProperty("expiresOn", UserCache.a.format(usercache_usercacheentry.b()));
            return jsonobject;
        }

        public UserCache.UserCacheEntry a(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
            if (jsonelement.isJsonObject()) {
                JsonObject jsonobject = jsonelement.getAsJsonObject();
                JsonElement jsonelement1 = jsonobject.get("name");
                JsonElement jsonelement2 = jsonobject.get("uuid");
                JsonElement jsonelement3 = jsonobject.get("expiresOn");

                if (jsonelement1 != null && jsonelement2 != null) {
                    String s = jsonelement2.getAsString();
                    String s1 = jsonelement1.getAsString();
                    Date date = null;

                    if (jsonelement3 != null) {
                        try {
                            date = UserCache.a.parse(jsonelement3.getAsString());
                        } catch (ParseException parseexception) {
                            date = null;
                        }
                    }

                    if (s1 != null && s != null) {
                        UUID uuid;

                        try {
                            uuid = UUID.fromString(s);
                        } catch (Throwable throwable) {
                            return null;
                        }

                        return UserCache.this.new UserCacheEntry(new GameProfile(uuid, s1), date, null);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        public JsonElement serialize(UserCacheEntry object, Type type, JsonSerializationContext jsonserializationcontext) { // CraftBukkit - decompile error
            return this.a(object, type, jsonserializationcontext);
        }

        public UserCacheEntry deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException { // CraftBukkit - decompile error
            return this.a(jsonelement, type, jsondeserializationcontext);
        }

        BanEntrySerializer(Object object) {
            this();
        }
    }
}