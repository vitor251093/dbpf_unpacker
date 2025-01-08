package sporemodder.util;

import java.io.InputStream;

public enum PackageSignature {
    NONE {
        @Override
        public String toString() {
            return "None";
        }

        @Override
        public String getFileName() {
            return null;
        }

        @Override
        public InputStream getInputStream() {
            return null;
        }
    },
    PATCH51 {
        @Override
        public String toString() {
            return "GA Patch 5.1";
        }

        @Override
        public String getFileName() {
            return "ExpansionPack1";
        }

        @Override
        public InputStream getInputStream() {
            return Project.class.getResourceAsStream("/sporemodder/resources/ExpansionPack1.prop");
        }
    },
    BOT_PARTS {
        @Override
        public String toString() {
            return "Bot Parts";
        }

        @Override
        public String getFileName() {
            return "BoosterPack2";
        }

        @Override
        public InputStream getInputStream() {
            return Project.class.getResourceAsStream("/sporemodder/resources/BoosterPack2.prop");
        }
    };

    public abstract String getFileName();

    public abstract InputStream getInputStream();
}
