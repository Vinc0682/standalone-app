/*
 * Copyright 2017 Sam Sun <github-contact@samczsun.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.heliosdecompiler.helios.transformers;

import com.heliosdecompiler.helios.Constants;
import com.heliosdecompiler.helios.Settings;
import com.heliosdecompiler.helios.transformers.assemblers.KrakatauAssembler;
import com.heliosdecompiler.helios.transformers.assemblers.SmaliAssembler;
import com.heliosdecompiler.helios.transformers.compilers.Compiler;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Transformer {
    private static final Pattern LEGAL_ID_PATTERN = Pattern.compile("[^0-9a-z\\-\\._]");
    // Only allow digits, lower case letters, and the following characters
    // - (HYPHEN)
    // . (PERIOD)
    // _ (UNDERSCORE)

    private static final Map<String, Transformer> BY_ID = new LinkedHashMap<>();
    private static final Map<String, Transformer> BY_NAME = new LinkedHashMap<>();

    static {
        Transformer0.registerAll();
    }

    protected final TransformerSettings settings = new TransformerSettings(this);
    private final String id;
    private final String name;
    protected Transformer(String id, String name) {
        this(id, name, null);
    }

    protected Transformer(String id, String name, Class<? extends TransformerSettings.Setting> settingsClass) {
        checkLegalId(id);
        checkLegalName(name);
        this.id = id;
        this.name = name;
        if (settingsClass != null) {
            if (settingsClass.isEnum()) {
                for (TransformerSettings.Setting setting : settingsClass.getEnumConstants()) {
                    getSettings().registerSetting(setting);
                }
            } else {
                throw new IllegalArgumentException("Settings must be an enum");
            }
        }
    }

    public static Transformer getById(String id) {
        return BY_ID.get(id);
    }

    public static Transformer getByName(String name) {
        return BY_NAME.get(name);
    }

    public static Collection<Transformer> getAllTransformers() {
        return getAllTransformers(transformer -> true);
    }

    public static Collection<Transformer> getAllTransformers(Predicate<Transformer> filter) {
        return BY_ID.values().stream().filter(filter).collect(Collectors.toList());
    }

    protected Transformer register() {
        if (BY_ID.containsKey(getId())) {
            throw new IllegalArgumentException(getId() + " already exists!");
        }
        if (BY_NAME.containsKey(getName())) {
            throw new IllegalArgumentException(getName() + " already exists!");
        }
        BY_ID.put(getId(), this);
        BY_NAME.put(getName(), this);
        return this;
    }

    public final TransformerSettings getSettings() {
        return this.settings;
    }

    public final String getId() {
        return this.id;
    }

    public final String getName() {
        return this.name;
    }

    public final boolean hasSettings() {
        return getSettings().size() > 0;
    }

    protected String parseException(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        e.printStackTrace();
        String exception = Constants.REPO_NAME + " version " + Constants.REPO_VERSION + "\n" + sw.toString();
        return "An exception occured while performing this task. Please open a GitHub issue with the details below.\n\n" + exception;
    }

    @Deprecated
    protected byte[] fixBytes(byte[] in) {
//        ClassReader reader = new ClassReader(in);
//        ClassNode node = new ClassNode();
//        reader.accept(node, ClassReader.EXPAND_FRAMES);
//        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//        node.accept(writer);
//        return writer.toByteArray();
        return in; // TODO: Report to author of decompiler
    }

    public abstract Object transform(Object... args);

    // Should be singletons
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    private void checkLegalId(String request) {
        if (request == null || request.length() == 0) throw new IllegalArgumentException("ID must not be empty");
        Matcher matcher = LEGAL_ID_PATTERN.matcher(request);
        if (matcher.find()) {
            throw new IllegalArgumentException("ID must only be lowercase letters and numbers");
        }
    }

    private void checkLegalName(String request) {
        if (request == null || request.length() == 0) throw new IllegalArgumentException("Name must not be empty");
    }

    private static class Transformer0 {
        private static void registerAll() {
            //assemblers
            new KrakatauAssembler().register();
            new SmaliAssembler().register();
            Compiler.getAllCompilers();
        }
    }
}
