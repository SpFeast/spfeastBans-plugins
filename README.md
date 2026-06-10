# spfeastBans Purpur 插件

spfeastBans 是一个面向 **Paper / Purpur 1.21+** 的封禁与禁言插件，保留了原项目里的 Hypixel 风格处罚文案，并补全了历史查询、分页展示、上线通知和 YAML 持久化。

## 功能概览
- 支持封禁：永久封禁、临时封禁、解封、信息查询、活动处罚列表
- 支持禁言：临时禁言、解禁、信息查询、聊天拦截提示
- 支持历史：合并 `ban` / `mute` 历史并按时间倒序分页显示
- 支持离线处罚：离线临时 ban 在玩家下次登录时才开始计时
- 支持在线即时反馈：在线玩家被 ban 时立即踢出，被 mute 时立即收到提示
- 支持 Hypixel 风格消息：处罚提示、列表头部、分页按钮、历史展示保持统一风格

## 当前命令

### 封禁
- `/ban <玩家> <模板> [原因...]`
- `/tempban <玩家> <模板> <时长> [原因...]`
- `/unban <玩家|UUID>`
- `/baninfo <玩家|UUID>`
- `/banlist [type] [page]`

### 禁言
- `/tempmute <玩家> <reasonKey> [time]`
- `/unmute <玩家|UUID>`

### 历史
- `/history <player> [page]`

## 当前行为说明

### Ban
- 在线玩家被 ban 时会立即踢出
- 在线被临时 ban 时，处罚立刻开始计时
- 离线被临时 ban 时，处罚会先挂起，**玩家下次登录时才开始计时**
- 永久 ban 不受延迟激活逻辑影响，始终直接生效
- 玩家尝试登录时会显示对应模板踢出消息
- 临时 ban 到期后会自动清理

### Mute
- 在线玩家被 mute 时会立即收到禁言提示
- 离线玩家被 mute 时，会在**下次首次上线时提示一次**
- 被禁言玩家发言时会被拦截，并且**每次发言都会再次提示**
- 单纯后续再次上线不会重复触发那次“离线补推”提示
- 临时 mute 到期后会自动清理

### History / List
- `/banlist` 显示当前仍然生效的处罚，并支持分页与类型过滤
- `/history` 会合并 ban / mute 历史记录，按时间倒序显示
- `/history` 现在和 `/banlist` 一样按页展示，带页码和上下页按钮

## Ban 模板
- Ban 模板定义文件：[src/main/java/com/andyoctopus/spfeastbans/BanTemplates.java](src/main/java/com/andyoctopus/spfeastbans/BanTemplates.java)
- 保留原项目里的模板数据和占位符渲染方式

## Mute 原因
- Mute 原因定义文件：[src/main/java/com/andyoctopus/spfeastbans/mute/MuteReason.java](src/main/java/com/andyoctopus/spfeastbans/mute/MuteReason.java)
- 当前内置：
  - `underreview`
  - `minorchat`
  - `majorchat`

## 数据文件
插件运行后会在 `plugins/spfeastBans/` 下生成和维护以下文件：

- `bans.yml`：当前生效 ban
- `bans-history.yml`：ban 历史记录
- `mutes.yml`：当前生效 mute
- `mutes-history.yml`：mute 历史记录
- `config.yml`：基础配置

## 配置项
当前 `config.yml` 中包含：

- `default-reason-fallback`：未提供原因时的默认文案
- `permanent-text`：永久处罚显示文本
- `date-format`：时间格式

当前默认值：

```yml
default-reason-fallback: "No reason provided"
permanent-text: "permanent"
date-format: "yyyy-MM-dd HH:mm"
```

## 时长格式
支持复合时长，例如：

```text
7d
12h30m
30d12h5m10s
2w3d
```

支持单位：
- `w` 周
- `d` 天
- `h` 小时
- `m` 分钟
- `s` 秒

## 权限
- `spfeastbans.ban`
- `spfeastbans.tempban`
- `spfeastbans.unban`
- `spfeastbans.tempmute`
- `spfeastbans.unmute`
- `spfeastbans.info`
- `spfeastbans.list`
- `spfeastbans.history`
- `spfeastbans.staffview`

默认均为 `op`。

## 构建
需要 JDK 21。

### 使用 Gradle Wrapper

```bash
./gradlew build
```

Windows:

```powershell
.\gradlew.bat build
```

### 产物
- Gradle 默认产物名称：`spfeastbans-purpur-<version>.jar`
- 当前项目版本：`2.0.1-SNAPSHOT`

## 兼容性
- 平台：Paper / Purpur
- 版本：1.21 及以上

