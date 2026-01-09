package com.live.service;

import com.live.dto.DebateFlowConfig;
import com.live.dto.JudgesConfig;
import com.live.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MockDataService {
    
    private final Map<String, Stream> streams = new ConcurrentHashMap<>();
    private final Map<String, VoteData> streamVotes = new ConcurrentHashMap<>();
    private final Map<String, JudgesConfig> judgesConfigs = new ConcurrentHashMap<>();
    private final Map<String, DebateFlowConfig> debateFlowConfigs = new ConcurrentHashMap<>();
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Debate> debates = new ConcurrentHashMap<>();
    private final Map<String, String> streamDebates = new ConcurrentHashMap<>();
    private final Map<String, List<AiContent>> aiContents = new ConcurrentHashMap<>();
    private final Map<String, Map<String, UserVoteRecord>> userVotes = new ConcurrentHashMap<>();
    private final Map<String, LiveStatus> liveStatuses = new ConcurrentHashMap<>();
    private final Map<String, String> aiStatuses = new ConcurrentHashMap<>();
    private final Map<String, Integer> viewers = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        initStreams();
        initVotes();
        initJudges();
        initDebateFlows();
        initUsers();
        initDebates();
        initAiContents();
        initLiveStatuses();
        initViewers();
        log.info("Mock数据初始化完成");
    }
    
    private void initStreams() {
        Stream stream1 = Stream.builder()
            .id("stream-1")
            .name("辩论赛直播间1")
            .url("rtmp://live.example.com/stream1")
            .type("debate")
            .enabled(true)
            .description("第一场辩论赛直播")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        Stream stream2 = Stream.builder()
            .id("stream-2")
            .name("辩论赛直播间2")
            .url("rtmp://live.example.com/stream2")
            .type("debate")
            .enabled(true)
            .description("第二场辩论赛直播")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        Stream stream3 = Stream.builder()
            .id("stream-3")
            .name("决赛直播间")
            .url("rtmp://live.example.com/final")
            .type("debate")
            .enabled(true)
            .description("决赛直播")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        streams.put(stream1.getId(), stream1);
        streams.put(stream2.getId(), stream2);
        streams.put(stream3.getId(), stream3);
    }
    
    private void initVotes() {
        streams.keySet().forEach(streamId -> {
            VoteData voteData = VoteData.builder()
                .streamId(streamId)
                .leftVotes(1000 + new Random().nextInt(5000))
                .rightVotes(1000 + new Random().nextInt(5000))
                .updatedAt(LocalDateTime.now())
                .build();
            voteData.calculatePercentages();
            streamVotes.put(streamId, voteData);
        });
    }
    
    private void initJudges() {
        streams.keySet().forEach(streamId -> {
            List<Judge> judges = Arrays.asList(
                Judge.builder()
                    .id("judge-1")
                    .name("张教授")
                    .role("主评委")
                    .avatar("")
                    .leftVotes(60)
                    .rightVotes(40)
                    .build(),
                Judge.builder()
                    .id("judge-2")
                    .name("李老师")
                    .role("嘉宾评委")
                    .avatar("")
                    .leftVotes(50)
                    .rightVotes(50)
                    .build(),
                Judge.builder()
                    .id("judge-3")
                    .name("王专家")
                    .role("嘉宾评委")
                    .avatar("")
                    .leftVotes(45)
                    .rightVotes(55)
                    .build()
            );
            
            JudgesConfig config = JudgesConfig.builder()
                .streamId(streamId)
                .judges(judges)
                .updatedAt(LocalDateTime.now())
                .build();
            judgesConfigs.put(streamId, config);
        });
    }
    
    private void initDebateFlows() {
        streams.keySet().forEach(streamId -> {
            List<DebateSegment> segments = Arrays.asList(
                DebateSegment.builder().name("正方立论").duration(180).side("left").order(1).build(),
                DebateSegment.builder().name("反方立论").duration(180).side("right").order(2).build(),
                DebateSegment.builder().name("正方质询").duration(120).side("left").order(3).build(),
                DebateSegment.builder().name("反方质询").duration(120).side("right").order(4).build(),
                DebateSegment.builder().name("自由辩论").duration(300).side("both").order(5).build(),
                DebateSegment.builder().name("正方总结").duration(120).side("left").order(6).build(),
                DebateSegment.builder().name("反方总结").duration(120).side("right").order(7).build()
            );
            
            DebateFlowConfig config = DebateFlowConfig.builder()
                .streamId(streamId)
                .flow(segments)
                .updatedAt(LocalDateTime.now())
                .build();
            debateFlowConfigs.put(streamId, config);
        });
    }
    
    private void initUsers() {
        for (int i = 1; i <= 10; i++) {
            User user = User.builder()
                .id("user-" + i)
                .nickname("用户" + i)
                .avatar("")
                .role(i <= 3 ? "judge" : "viewer")
                .createdAt(LocalDateTime.now())
                .build();
            users.put(user.getId(), user);
        }
    }

    private void initDebates() {
        streams.values().forEach(stream -> {
            Debate debate = Debate.builder()
                .id("debate-" + stream.getId())
                .streamId(stream.getId())
                .title("如果有一个能一键消除痛苦的按钮，你会按吗？")
                .description("这是一个关于痛苦、成长与人性选择的深度辩论")
                .leftPosition("支持按下按钮")
                .rightPosition("反对按下按钮")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            debates.put(debate.getId(), debate);
            streamDebates.put(stream.getId(), debate.getId());
        });
    }

    private void initAiContents() {
        streams.values().forEach(stream -> {
            String debateId = streamDebates.get(stream.getId());
            List<AiContent> contents = new ArrayList<>();
            AiContent content1 = AiContent.builder()
                .id(UUID.randomUUID().toString())
                .debateId(debateId)
                .streamId(stream.getId())
                .text("正方观点：痛苦是成长的一部分，消除痛苦可能让人失去进步动力。")
                .side("left")
                .timestamp(System.currentTimeMillis() - 300000)
                .likes(45)
                .build();
            content1.getComments().add(AiComment.builder()
                .id(UUID.randomUUID().toString())
                .contentId(content1.getId())
                .user("心理学家")
                .avatar("🧠")
                .text("痛苦确实能促进成长，但过度痛苦会造成创伤。")
                .likes(12)
                .timestamp(System.currentTimeMillis() - 240000)
                .build());

            AiContent content2 = AiContent.builder()
                .id(UUID.randomUUID().toString())
                .debateId(debateId)
                .streamId(stream.getId())
                .text("反方观点：如果能消除痛苦，为什么不呢？我们可以更专注积极生活。")
                .side("right")
                .timestamp(System.currentTimeMillis() - 240000)
                .likes(52)
                .build();
            content2.getComments().add(AiComment.builder()
                .id(UUID.randomUUID().toString())
                .contentId(content2.getId())
                .user("医生")
                .avatar("👨‍⚕️")
                .text("临床中看到的痛苦太多，若能减少，我支持。")
                .likes(18)
                .timestamp(System.currentTimeMillis() - 200000)
                .build());

            contents.add(content1);
            contents.add(content2);
            aiContents.put(stream.getId(), contents);
        });
    }

    private void initLiveStatuses() {
        streams.values().forEach(stream -> {
            liveStatuses.put(stream.getId(), LiveStatus.builder()
                .streamId(stream.getId())
                .isLive(false)
                .streamUrl(stream.getUrl())
                .liveId(null)
                .startTime(0L)
                .build());
            aiStatuses.put(stream.getId(), "stopped");
        });
    }

    private void initViewers() {
        Random random = new Random();
        streams.values().forEach(stream -> viewers.put(stream.getId(), 50 + random.nextInt(200)));
    }
    
    // Streams
    public List<Stream> getAllStreams() {
        List<Stream> result = new ArrayList<>();
        streams.values().forEach(stream -> result.add(enrichStream(stream)));
        return result;
    }

    public String getDefaultStreamId() {
        return streams.values().stream()
            .filter(Stream::isEnabled)
            .map(Stream::getId)
            .findFirst()
            .orElseGet(() -> streams.keySet().stream().findFirst().orElse("stream-1"));
    }
    
    public Optional<Stream> getStream(String id) {
        Stream stream = streams.get(id);
        return stream == null ? Optional.empty() : Optional.of(enrichStream(stream));
    }

    public Stream createStream(Stream input) {
        String id = input.getId() != null ? input.getId() : "stream-" + UUID.randomUUID();
        Stream stream = Stream.builder()
            .id(id)
            .name(input.getName() != null ? input.getName() : "新直播流")
            .url(input.getUrl() != null ? input.getUrl() : "rtmp://live.example.com/" + id)
            .type(input.getType() != null ? input.getType() : "debate")
            .enabled(input.isEnabled())
            .description(input.getDescription())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        streams.put(id, stream);
        initStreamDefaults(stream);
        return enrichStream(stream);
    }

    public Stream updateStream(String id, Stream update) {
        Stream stream = streams.get(id);
        if (stream == null) {
            return null;
        }
        if (update.getName() != null) {
            stream.setName(update.getName());
        }
        if (update.getUrl() != null) {
            stream.setUrl(update.getUrl());
        }
        if (update.getType() != null) {
            stream.setType(update.getType());
        }
        if (update.getDescription() != null) {
            stream.setDescription(update.getDescription());
        }
        stream.setEnabled(update.isEnabled());
        stream.setUpdatedAt(LocalDateTime.now());
        return enrichStream(stream);
    }

    public Stream toggleStream(String id) {
        Stream stream = streams.get(id);
        if (stream == null) {
            return null;
        }
        stream.setEnabled(!stream.isEnabled());
        stream.setUpdatedAt(LocalDateTime.now());
        return enrichStream(stream);
    }

    public boolean deleteStream(String id) {
        Stream removed = streams.remove(id);
        if (removed == null) {
            return false;
        }
        streamVotes.remove(id);
        judgesConfigs.remove(id);
        debateFlowConfigs.remove(id);
        aiContents.remove(id);
        liveStatuses.remove(id);
        aiStatuses.remove(id);
        viewers.remove(id);
        streamDebates.remove(id);
        return true;
    }
    
    // Votes
    public VoteData getVotes(String streamId) {
        return streamVotes.computeIfAbsent(streamId, id -> {
            VoteData voteData = VoteData.builder()
                .streamId(id)
                .leftVotes(0)
                .rightVotes(0)
                .updatedAt(LocalDateTime.now())
                .build();
            voteData.calculatePercentages();
            return voteData;
        });
    }
    
    public VoteData updateVotes(String streamId, Integer leftVotes, Integer rightVotes, String action) {
        VoteData voteData = getVotes(streamId);
        
        if ("add".equalsIgnoreCase(action)) {
            voteData.setLeftVotes(voteData.getLeftVotes() + (leftVotes != null ? leftVotes : 0));
            voteData.setRightVotes(voteData.getRightVotes() + (rightVotes != null ? rightVotes : 0));
        } else {
            if (leftVotes != null) voteData.setLeftVotes(leftVotes);
            if (rightVotes != null) voteData.setRightVotes(rightVotes);
        }
        
        voteData.calculatePercentages();
        voteData.setUpdatedAt(LocalDateTime.now());
        return voteData;
    }
    
    public VoteData addUserVote(String streamId, Integer leftVotes, Integer rightVotes) {
        VoteData voteData = getVotes(streamId);
        voteData.setLeftVotes(voteData.getLeftVotes() + (leftVotes != null ? leftVotes : 0));
        voteData.setRightVotes(voteData.getRightVotes() + (rightVotes != null ? rightVotes : 0));
        voteData.calculatePercentages();
        voteData.setUpdatedAt(LocalDateTime.now());
        return voteData;
    }
    
    // Judges
    public JudgesConfig getJudges(String streamId) {
        return judgesConfigs.computeIfAbsent(streamId, id -> 
            JudgesConfig.builder()
                .streamId(id)
                .judges(new ArrayList<>())
                .updatedAt(LocalDateTime.now())
                .build()
        );
    }
    
    public JudgesConfig saveJudges(String streamId, List<Judge> judges) {
        JudgesConfig config = JudgesConfig.builder()
            .streamId(streamId)
            .judges(judges)
            .updatedAt(LocalDateTime.now())
            .build();
        judgesConfigs.put(streamId, config);
        return config;
    }
    
    // Debate Flow
    public DebateFlowConfig getDebateFlow(String streamId) {
        return debateFlowConfigs.computeIfAbsent(streamId, id ->
            DebateFlowConfig.builder()
                .streamId(id)
                .flow(new ArrayList<>())
                .updatedAt(LocalDateTime.now())
                .build()
        );
    }
    
    public DebateFlowConfig saveDebateFlow(String streamId, List<DebateSegment> flow) {
        DebateFlowConfig config = DebateFlowConfig.builder()
            .streamId(streamId)
            .flow(flow)
            .updatedAt(LocalDateTime.now())
            .build();
        debateFlowConfigs.put(streamId, config);
        return config;
    }
    
    // Users
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    public Optional<User> getUser(String id) {
        return Optional.ofNullable(users.get(id));
    }

    // Debates
    public Debate getDebateByStream(String streamId) {
        if (streamId == null) {
            return debates.values().stream().findFirst().orElse(null);
        }
        String debateId = streamDebates.get(streamId);
        return debateId == null ? null : debates.get(debateId);
    }

    public Debate getDebateById(String debateId) {
        return debateId == null ? null : debates.get(debateId);
    }

    public Debate createDebate(Debate debate) {
        String id = debate.getId() != null ? debate.getId() : "debate-" + UUID.randomUUID();
        Debate stored = Debate.builder()
            .id(id)
            .streamId(debate.getStreamId())
            .title(debate.getTitle())
            .description(debate.getDescription())
            .leftPosition(debate.getLeftPosition())
            .rightPosition(debate.getRightPosition())
            .isActive(debate.isActive())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        debates.put(id, stored);
        return stored;
    }

    public Debate updateDebate(String debateId, Debate update) {
        Debate debate = debates.get(debateId);
        if (debate == null) {
            return null;
        }
        if (update.getTitle() != null) {
            debate.setTitle(update.getTitle());
        }
        if (update.getDescription() != null) {
            debate.setDescription(update.getDescription());
        }
        if (update.getLeftPosition() != null) {
            debate.setLeftPosition(update.getLeftPosition());
        }
        if (update.getRightPosition() != null) {
            debate.setRightPosition(update.getRightPosition());
        }
        debate.setActive(update.isActive());
        debate.setUpdatedAt(LocalDateTime.now());
        return debate;
    }

    public Debate associateDebate(String streamId, String debateId) {
        if (streamId == null || debateId == null) {
            return null;
        }
        Debate debate = debates.get(debateId);
        if (debate == null) {
            return null;
        }
        streamDebates.put(streamId, debateId);
        debate.setStreamId(streamId);
        debate.setUpdatedAt(LocalDateTime.now());
        return debate;
    }

    public void clearDebateAssociation(String streamId) {
        if (streamId != null) {
            streamDebates.remove(streamId);
        }
    }

    // AI content
    public List<AiContent> getAiContents(String streamId) {
        if (streamId == null) {
            List<AiContent> all = new ArrayList<>();
            aiContents.values().forEach(all::addAll);
            return all;
        }
        return new ArrayList<>(aiContents.getOrDefault(streamId, new ArrayList<>()));
    }

    public AiContent addAiContent(String streamId, AiContent content) {
        String effectiveStreamId = streamId != null ? streamId : getDefaultStreamId();
        aiContents.putIfAbsent(effectiveStreamId, new ArrayList<>());
        AiContent created = AiContent.builder()
            .id(content.getId() != null ? content.getId() : UUID.randomUUID().toString())
            .debateId(content.getDebateId())
            .streamId(effectiveStreamId)
            .text(content.getText())
            .side(content.getSide())
            .timestamp(System.currentTimeMillis())
            .likes(content.getLikes())
            .comments(content.getComments() != null ? content.getComments() : new ArrayList<>())
            .build();
        aiContents.get(effectiveStreamId).add(created);
        return created;
    }

    public AiContent updateAiContent(String contentId, AiContent update) {
        AiContent existing = getAiContentById(contentId);
        if (existing == null) {
            return null;
        }
        if (update.getText() != null) {
            existing.setText(update.getText());
        }
        if (update.getSide() != null) {
            existing.setSide(update.getSide());
        }
        if (update.getDebateId() != null) {
            existing.setDebateId(update.getDebateId());
        }
        return existing;
    }

    public AiContent getAiContentById(String contentId) {
        if (contentId == null) {
            return null;
        }
        for (List<AiContent> list : aiContents.values()) {
            for (AiContent content : list) {
                if (contentId.equals(content.getId())) {
                    return content;
                }
            }
        }
        return null;
    }

    public boolean deleteAiContent(String contentId) {
        if (contentId == null) {
            return false;
        }
        for (Map.Entry<String, List<AiContent>> entry : aiContents.entrySet()) {
            List<AiContent> list = entry.getValue();
            boolean removed = list.removeIf(item -> contentId.equals(item.getId()));
            if (removed) {
                return true;
            }
        }
        return false;
    }

    public AiComment addComment(String contentId, String text, String user, String avatar) {
        AiContent content = getAiContentById(contentId);
        if (content == null) {
            return null;
        }
        AiComment comment = AiComment.builder()
            .id(UUID.randomUUID().toString())
            .contentId(contentId)
            .user(user)
            .avatar(avatar)
            .text(text)
            .likes(0)
            .timestamp(System.currentTimeMillis())
            .build();
        content.getComments().add(comment);
        return comment;
    }

    public AiComment deleteComment(String contentId, String commentId) {
        AiContent content = getAiContentById(contentId);
        if (content == null) {
            return null;
        }
        Iterator<AiComment> iterator = content.getComments().iterator();
        while (iterator.hasNext()) {
            AiComment comment = iterator.next();
            if (commentId.equals(comment.getId())) {
                iterator.remove();
                return comment;
            }
        }
        return null;
    }

    public AiContent likeContent(String contentId, String commentId) {
        AiContent content = getAiContentById(contentId);
        if (content == null) {
            return null;
        }
        if (commentId == null) {
            content.setLikes(content.getLikes() + 1);
            return content;
        }
        for (AiComment comment : content.getComments()) {
            if (commentId.equals(comment.getId())) {
                comment.setLikes(comment.getLikes() + 1);
                return content;
            }
        }
        return content;
    }

    // User votes
    public UserVoteRecord getUserVote(String streamId, String userId) {
        if (streamId == null || userId == null) {
            return null;
        }
        Map<String, UserVoteRecord> streamVotes = userVotes.get(streamId);
        if (streamVotes == null) {
            return null;
        }
        return streamVotes.get(userId);
    }

    public UserVoteRecord recordUserVote(String streamId, String userId, int leftVotes, int rightVotes) {
        if (streamId == null || userId == null) {
            return null;
        }
        userVotes.putIfAbsent(streamId, new ConcurrentHashMap<>());
        UserVoteRecord record = UserVoteRecord.builder()
            .streamId(streamId)
            .userId(userId)
            .leftVotes(leftVotes)
            .rightVotes(rightVotes)
            .updatedAt(System.currentTimeMillis())
            .build();
        userVotes.get(streamId).put(userId, record);
        return record;
    }

    // Live status / AI status / viewers
    public LiveStatus getLiveStatus(String streamId) {
        if (streamId != null) {
            return liveStatuses.get(streamId);
        }
        return liveStatuses.values().stream().findFirst().orElse(null);
    }

    public LiveStatus startLive(String streamId) {
        Stream stream = streams.get(streamId);
        if (stream == null) {
            return null;
        }
        LiveStatus status = liveStatuses.get(streamId);
        if (status == null) {
            status = LiveStatus.builder().streamId(streamId).build();
        }
        status.setLive(true);
        status.setStreamUrl(stream.getUrl());
        status.setLiveId(UUID.randomUUID().toString());
        status.setStartTime(System.currentTimeMillis());
        liveStatuses.put(streamId, status);
        return status;
    }

    public LiveStatus stopLive(String streamId) {
        LiveStatus status = liveStatuses.get(streamId);
        if (status == null) {
            return null;
        }
        status.setLive(false);
        status.setLiveId(null);
        status.setStartTime(0L);
        liveStatuses.put(streamId, status);
        return status;
    }

    public String getAiStatus(String streamId) {
        return aiStatuses.getOrDefault(streamId, "stopped");
    }

    public String setAiStatus(String streamId, String status) {
        if (streamId == null) {
            return null;
        }
        aiStatuses.put(streamId, status);
        return status;
    }

    public int getViewers(String streamId) {
        if (streamId == null) {
            return 0;
        }
        viewers.putIfAbsent(streamId, 10);
        return viewers.get(streamId);
    }

    public Map<String, Integer> getAllViewers() {
        return new HashMap<>(viewers);
    }

    public int bumpViewers(String streamId) {
        int current = getViewers(streamId);
        int next = Math.max(0, current + new Random().nextInt(5) - 2);
        viewers.put(streamId, next);
        return next;
    }

    private void initStreamDefaults(Stream stream) {
        String streamId = stream.getId();
        streamVotes.putIfAbsent(streamId, VoteData.builder()
            .streamId(streamId)
            .leftVotes(0)
            .rightVotes(0)
            .updatedAt(LocalDateTime.now())
            .build());
        judgesConfigs.putIfAbsent(streamId, JudgesConfig.builder()
            .streamId(streamId)
            .judges(new ArrayList<>())
            .updatedAt(LocalDateTime.now())
            .build());
        debateFlowConfigs.putIfAbsent(streamId, DebateFlowConfig.builder()
            .streamId(streamId)
            .flow(new ArrayList<>())
            .updatedAt(LocalDateTime.now())
            .build());
        aiContents.putIfAbsent(streamId, new ArrayList<>());
        liveStatuses.putIfAbsent(streamId, LiveStatus.builder()
            .streamId(streamId)
            .isLive(false)
            .streamUrl(stream.getUrl())
            .startTime(0L)
            .build());
        aiStatuses.putIfAbsent(streamId, "stopped");
        viewers.putIfAbsent(streamId, 10);
        Debate debate = Debate.builder()
            .id("debate-" + streamId)
            .streamId(streamId)
            .title("默认辩题")
            .description("请在后台管理中修改辩题内容")
            .leftPosition("正方观点")
            .rightPosition("反方观点")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        debates.putIfAbsent(debate.getId(), debate);
        streamDebates.putIfAbsent(streamId, debate.getId());
    }

    private Stream enrichStream(Stream stream) {
        LiveStatus status = liveStatuses.get(stream.getId());
        stream.setLiveStatus(status);
        return stream;
    }
}
