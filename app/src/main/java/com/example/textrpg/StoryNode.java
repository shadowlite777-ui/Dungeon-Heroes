package com.example.textrpg;

import java.util.ArrayList;
import java.util.List;

public class StoryNode {
    private String id;
    private String text;
    private List<Option> options;

    public StoryNode(String id, String text) {
        this.id = id;
        this.text = text;
        this.options = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void addOption(String text, String targetNodeId) {
        options.add(new Option(text, targetNodeId));
    }

    public static class Option {
        private String text;
        private String targetNodeId;

        public Option(String text, String targetNodeId) {
            this.text = text;
            this.targetNodeId = targetNodeId;
        }

        public String getText() {
            return text;
        }

        public String getTargetNodeId() {
            return targetNodeId;
        }
    }
}
