# spfeastBans Purpur 插件

spfeastBans 已重构为 **Paper / Purpur 1.21+ 的真实封禁插件**，并保留原项目里的封禁模板文案。

## 当前功能
- `/ban <玩家> <模板> [原因...]`
- `/tempban <玩家> <模板> <时长> [原因...]`
- `/unban <玩家|UUID>`
- `/baninfo <玩家|UUID>`
- 使用 `plugins/spfeastBans/bans.yml` 持久化封禁记录
- 在线玩家被封时立即踢出
- 被封玩家重登时继续显示同一套模板文案
- 临时封禁到期后自动清理

## 模板
模板仍在 `src/main/java/com/andyoctopus/spfeastbans/BanTemplates.java` 中维护，沿用原项目的数据与占位符渲染方式。

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

## 构建
需要 JDK 21。

```bash
./gradlew build
```

## 兼容性
- 平台：Paper / Purpur
- 版本：1.21 及以上
- 不再支持 Forge 1.8.9 客户端 Mod

