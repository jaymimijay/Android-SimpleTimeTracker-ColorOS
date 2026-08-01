# SimpleTimeTracker + OPPO 流体云 — 快速部署包

> 本包包含所有新增文件 + 现有文件的精确修改步骤。
> 你只需 Fork 原仓库 → 上传 3 个新文件 → 改 3 个现有文件 → 触发编译。

---

## 📦 包内文件说明

```
新增文件上传路径/                          ← 这 3 个文件直接上传到你的 Fork
├── features/feature_notification/.../fluidCloud/FluidCloudHelper.kt
├── features/feature_notification/.../fluidCloud/FluidCloudBroadcastReceiver.kt
└── .github/workflows/build_fluid_cloud.yml

修改现有文件步骤.md                        ← 告诉你现有 3 个文件各自改哪里
```

---

## 🚀 第一步：Fork 原仓库

1. 打开 https://github.com/Razeeman/Android-SimpleTimeTracker
2. 点击右上角 **Fork** 按钮
3. 等待几秒，进入你自己的 Fork 仓库（地址类似 `github.com/你的用户名/Android-SimpleTimeTracker`）

---

## 📁 第二步：上传 3 个新增文件（1 分钟）

### 方法：GitHub 网页批量上传

1. 在你的 Fork 仓库主页，点击 **Add file** → **Upload files**
2. 把本包 `新增文件上传路径/` 下的 3 个文件，**保持目录结构**拖进去：
   - `features/feature_notification/src/main/java/com/razeeman/util/simpletimetracker/feature_notification/fluidCloud/FluidCloudHelper.kt`
   - `features/feature_notification/src/main/java/com/razeeman/util/simpletimetracker/feature_notification/fluidCloud/FluidCloudBroadcastReceiver.kt`
   - `.github/workflows/build_fluid_cloud.yml`
3. 页面底部写提交信息：`Add Fluid Cloud support`
4. 选择 **Commit directly to the dev branch**
5. 点击 **Commit changes**

> 💡 提示：GitHub 网页上传会自动创建不存在的目录，你只需要确保文件路径正确。

---

## ✏️ 第三步：修改 3 个现有文件（2 分钟）

### 文件 1：features/feature_notification/build.gradle.kts

**操作**：在你的 Fork 中，点击该文件 → 铅笔图标（Edit）

**查找**（按 Ctrl+F 搜索）：
```
compileSdk = 35
```

**替换为**：
```
compileSdk = 36
```

**提交**：页面底部写 `Update compileSdk to 36`，Commit

---

### 文件 2：app/build.gradle.kts

**操作**：同上，点击该文件 → 铅笔图标

**查找**：
```
compileSdk = 35
```

**替换为**：
```
compileSdk = 36
```

**提交**

---

### 文件 3：app/src/main/AndroidManifest.xml

**操作**：点击该文件 → 铅笔图标

**第 1 处修改：添加通知权限**

在 `<manifest>` 标签内（通常在文件开头，`<application>` 标签上方），找到第一个空白行或已有权限的下方，添加：

```xml
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

**第 2 处修改：添加广播接收器**

在 `<application>` 标签内（找到最后一个 `</activity>` 或 `</service>` 之后，`</application>` 之前），添加：

```xml
        <receiver
            android:name=".feature_notification.fluidCloud.FluidCloudBroadcastReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="com.razeeman.util.simpletimetracker.ACTION_START_ACTIVITY" />
                <action android:name="com.razeeman.util.simpletimetracker.ACTION_STOP_ACTIVITY" />
                <action android:name="com.razeeman.util.simpletimetracker.ACTION_STOP_ALL_ACTIVITIES" />
            </intent-filter>
        </receiver>
```

**提交**

---

## ⚙️ 第四步：触发自动编译

1. 在你的 Fork 仓库，点击顶部 **Actions** 菜单
2. 左侧列表中找到 **Build with Fluid Cloud (OPPO)**
3. 点击右侧 **Run workflow** 下拉按钮
4. 选择分支 `dev`（或 `main`/`master`）
5. 点击绿色 **Run workflow**
6. 等待 5-10 分钟

---

## 📥 第五步：下载 APK

1. 编译完成后，点击进入最新的运行记录
2. 页面底部 **Artifacts** 区域
3. 点击 **simple-time-tracker-fluid-cloud-apk** 下载 ZIP
4. 解压 ZIP，得到 APK，传到手机安装

> ⚠️ 安装前在手机设置中允许"安装未知来源应用"。

---

## 📱 使用说明

1. 安装 APK，授予**通知权限**
2. 开始计时任意活动
3. **ColorOS 16+** 设备：状态栏自动显示流体云胶囊，展示活动名和已用时间
4. 长按胶囊展开卡片，点击卡片回到应用
5. 停止计时，流体云自动消失
6. **旧系统**：回退为普通持续通知，功能完全正常

---

## ❓ 常见问题

**Q: 编译报错 "compileSdkVersion 36 not found"**
A: 确认两个 build.gradle.kts 都已改为 `compileSdk = 36`。工作流已配置自动安装 API 36。

**Q: 编译报错 "cannot find symbol BAKLAVA"**
A: 确认 compileSdk 已改为 36。BAKLAVA 是 Android 16 的代号。

**Q: 安装后流体云没显示**
A: 确认系统是 ColorOS 16+（基于 Android 16），且已授予通知权限。ColorOS 14/15 不支持此方案。

**Q: Actions 页面看不到工作流**
A: 确认 `.github/workflows/build_fluid_cloud.yml` 文件路径正确（注意前面的点 `.github`），且已提交到仓库。

---

## 🛡️ 安全说明

- 不收集任何用户数据
- 仅使用本地通知 API，无网络请求
- 零侵入设计，不修改现有业务逻辑
