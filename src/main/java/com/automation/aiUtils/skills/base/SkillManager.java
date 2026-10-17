package com.automation.aiUtils.skills.base;

import com.automation.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SkillManager {

    private static final Logger logger = LoggerFactory.getLogger(SkillManager.class);
    private static SkillManager instance;
    private final SkillContext context;
    private final ConcurrentMap<String, Skill> skills = new ConcurrentHashMap<>();
    private final List<Skill> initializationOrder = new ArrayList<>();

    private SkillManager(ConfigManager config) {
        this.context = new SkillContext(config);
        discoverAndLoadSkills();
        initAll();
    }

    public static synchronized SkillManager getInstance(ConfigManager config) {
        if (instance == null) {
            instance = new SkillManager(config);
        }
        return instance;
    }

    public static SkillManager getInstance() {
        if (instance == null) {
            instance = new SkillManager(ConfigManager.getInstance());
        }
        return instance;
    }

    private void discoverAndLoadSkills() {
        ServiceLoader<Skill> loader = ServiceLoader.load(Skill.class);
        for (Skill skill : loader) {
            logger.info("Discovered AIIntegration skill: {} ({})", skill.getSkillName(), skill.getSkillId());
            skills.put(skill.getSkillId(), skill);
            initializationOrder.add(skill);
        }
    }

    private void initAll() {
        for (Skill skill : initializationOrder) {
            try {
                skill.init(context);
                logger.info("Initialized AIIntegration skill: {} (enabled={})",
                        skill.getSkillId(), skill.isEnabled());
            } catch (Exception e) {
                logger.error("Failed to initialize AIIntegration skill: {}", skill.getSkillId(), e);
            }
        }
    }

    public Skill getSkill(String skillId) {
        return skills.get(skillId);
    }

    public List<Skill> getAllSkills() {
        return new ArrayList<>(initializationOrder);
    }

    public List<Skill> getEnabledSkills() {
        List<Skill> enabled = new ArrayList<>();
        for (Skill skill : initializationOrder) {
            if (skill.isEnabled()) {
                enabled.add(skill);
            }
        }
        return enabled;
    }

    public boolean isSkillEnabled(String skillId) {
        Skill skill = skills.get(skillId);
        return skill != null && skill.isEnabled();
    }

    public void destroyAll() {
        for (Skill skill : initializationOrder) {
            try {
                skill.destroy();
                logger.info("Destroyed AIIntegration skill: {}", skill.getSkillId());
            } catch (Exception e) {
                logger.error("Failed to destroy AIIntegration skill: {}", skill.getSkillId(), e);
            }
        }
    }

    public SkillContext getContext() {
        return context;
    }
}
