# Stellar Music 设计规范

各 Composable 的用途、完整参数、默认值与调用示例见 [Composable 使用手册](composable-reference.md)。本文负责说明设计语义与样式选择规则。

## 入口与边界

- `StellarMusicTheme` 安装 Material 主题，并跟随系统深浅模式；动态配色默认关闭。
- `StellarTheme` 是项目唯一的样式读取入口：`colors`、`shapes`、`spacing`、`sizes`、`typography`、`motion`。
- 页面及播放器状态层只调用 `ui/components` 中的视觉组件。允许直接使用 `Row`、`Column`、`Box`、`Spacer`、Lazy 布局、滚动和安全区 modifier。
- Material 视觉控件仅在组件实现或 Theme 内使用。页面不直接挑选颜色、圆角、字号，也不硬编码 dp/sp。
- 基础组件只处理展示和输入事件，不请求网络、不读取账号、不操作导航。业务层负责数据、状态和回调。
- 新组件先使用已有语义；确实缺少用途时再增加 Theme token，并同步更新本文。不要为单个页面复制一套外观配置。

## 颜色

所有语义值由当前 Material ColorScheme 映射，深浅主题分别由 `Color.kt` 提供。不要将下表的 Material 色槽名带入页面。

| 语义颜色 | 用途 | 配对前景／映射 |
| --- | --- | --- |
| `pageBackground` | 页面底色 | `textPrimary`；background |
| `cardBackground` | 表单、歌单、账号信息卡片 | `textPrimary`；surfaceContainerLow |
| `overlayBackground` | 弹窗、菜单、底部面板 | `textPrimary`；surfaceContainerLow |
| `insetBackground` | 卡片内分组、可点击选择行 | `textPrimary`；surfaceContainer |
| `playerBackground` | 底部播放器 | `textPrimary`；surfaceContainerHigh |
| `itemBackground` | 未选中的歌曲条目 | `textPrimary`；surface |
| `textPrimary` | 正文、名称 | onSurface |
| `textSecondary` | 歌手、数量、时间等辅助信息 | onSurfaceVariant；用于普通中性底色 |
| `accent` | 链接、文字操作、选中导航图标、进度 | primary |
| `primaryAction` | 主要操作背景 | **只配 `onPrimaryAction`**；primary / onPrimary |
| `secondaryAction` | 次要操作背景 | **只配 `onSecondaryAction`**；secondaryContainer / onSecondaryContainer |
| `selectedBackground` | 当前歌曲、选中行及导航指示 | **配 `onSelectedBackground`**；secondaryContainer / onSecondaryContainer |
| `dangerBackground` | 删除等危险操作的填充背景 | **只配 `onDangerBackground`**；errorContainer / onErrorContainer |
| `dangerForeground` | 无填充的危险提示或操作文字 | error；仅用于页面、卡片、浮层等中性底色 |
| `divider` | 条目之间的分隔线 | outlineVariant，不用于正文 |
| `outline` | 输入框等必要边界 | outline，不默认描边所有卡片 |
| `disabledBackground` | 不可用填充按钮背景 | onSurface，12% 不透明度 |
| `disabledContent` | 不可用按钮文字与图标 | onSurface，38% 不透明度 |
| `progressTrack` | 未播放的进度轨道 | secondary |
| `coverForeground` | 占位封面音符 | onPrimaryContainer |
| `overlayScrim` | 底部面板外部遮罩 | scrim，32% 不透明度 |
| `accentGradient` | 弹窗标题装饰 | primary → tertiary，左上到右下 |
| `coverGradient` | 占位封面背景 | primaryContainer → tertiaryContainer，左上到右下 |

`StellarTone` 将这些用途提供给文字、图标和加载指示器。默认 `Inherit` 继承所在容器的前景色：**按钮内的文字和图标保持 Inherit**，不要另设红色或主色，否则可能破坏前景／背景配对。`Secondary` 只用于辅助文字；`Accent` 不作为普通正文颜色。

渐变不用于正文、列表歌曲名称或大面积页面背景。本应用不显示页面大标题；弹窗仍保留说明操作所需的标题。

## 圆角

| `StellarTheme.shapes` | 用途 | 当前值 |
| --- | --- | --- |
| `card` | 表单、歌单、播放器卡片 | 24dp |
| `dialog` | 确认、编辑、选择弹窗 | 24dp |
| `menu` | 锚定更多菜单 | 24dp |
| `sheet` | 歌曲详情底部面板 | 24dp |
| `button` | 操作按钮 | 20dp |
| `textField` | 输入框 | 16dp |
| `cover` | 占位封面 | 20dp |
| `item` | 歌曲行、选择行及内部信息分组 | 20dp |
| `avatar` | 账号头像、播放器旋转封面裁剪 | 圆形 |

用途分别命名，即使数值相同也不要互相替代。例如修改 `menu` 不应该改变表单卡片。组件负责选取用途，页面不传 `shape`。

## 字体与文字

`StellarTextRole` 与 `StellarTheme.typography` 对应：

| role | Theme 字段 | 用途 |
| --- | --- | --- |
| `Inherit` | 当前容器文字样式 | 按钮、导航等拥有自身文字上下文的区域 |
| `Title` | `title` | 详情名称，22sp / 28sp 行高 |
| `ItemTitle` | `itemTitle` | 歌曲、歌单名称，16sp / 24sp |
| `Body` | `body` | 表单及正文，16sp / 24sp |
| `BodySmall` | `bodySmall` | 辅助正文，14sp / 20sp |
| `Action` | `action` | 操作标签、紧凑播放器歌名，14sp / 20sp |
| `Caption` | `caption` | 数量、时间、版本，12sp / 16sp |
| `Micro` | `micro` | 播放器辅助信息，11sp / 16sp |
| `Avatar` | `avatar` | 头像首字，不用于页面标题，32sp / 40sp |

`dialogTitleStyle` 在 Theme 内组合 `title` 字体与 `accentGradient`。字重、行高和字号仅在 Theme 定义。列表名称单行省略；播放器长歌名使用 `PlayerTrackTitle`，在两端往返，不复制或首尾循环拼接。

## 间距、尺寸与动画

- `spacing.page`：页面／表单外边距，20dp。
- `spacing.section`：常用卡片内边距及分组间距，16dp。
- `spacing.itemGap`：条目内部图文间距，12dp；`gap` 为 8dp，`small` 为 4dp，`tiny` 为 2dp。
- `spacing.trackVerticalPadding`：歌曲条目上下留白，8dp；`homeTrackMargin`：首页歌曲外边距，8dp。
- `sizes.touch`：按钮最小操作尺寸，48dp；视觉封面可以小于操作区。
- `sizes.hairline`：全宽歌曲分隔线，0.5dp；末项不显示。
- `CoverPlaceholderSize.Small / Medium / Large`：40 / 52 / 96dp；配套音符为 22 / 28 / 52dp。调用处选择枚举，Theme 决定具体数值。
- `sizes.seekHeight`：进度条区域，24dp；`progress`：轨道粗细，4dp；`thumb`：滑块半径，6dp。
- `sizes.cardElevation`：播放器与浮层色调高度，2dp；`menuElevation`：菜单阴影高度，0dp。菜单不添加额外边框。
- `sizes.loadingIndicator / indicator / indicatorStroke`：普通加载图标40dp、小图标18dp、小图标描边2dp。
- `sizes.loadingArea / dialogHeight`：弹窗加载区域最小120dp、列表最大360dp。
- `motion.coverRotationDurationMillis`：播放时12秒一圈；暂停保留角度，继续播放后接着旋转。
- `motion.titleScrollSpeed / titleScrollPauseMillis`：歌名每秒30dp，到两端各停顿1秒；短文本不滚动。

`spacious`、`large`、头像、菜单和内容宽度等补充 token 也集中在 `Dimensions.kt`。页面不得通过随意选择更大的 token 来重新引入已删除的标题占位。

## 组件选择

| 场景 | 组件／参数 |
| --- | --- |
| 主要操作 | `StellarButton(kind = Primary)` |
| 次要操作、弹窗保存 | `StellarButton(kind = Secondary)` |
| 描边操作／重试 | `StellarButton(kind = Outlined)` 或 `StellarRetryAction` |
| 取消、返回、编辑入口 | `StellarButton(kind = Text)` |
| 删除确认、菜单删除 | `StellarButton(kind = Danger)` |
| 加载中的操作 | `StellarButton(loading = true)`，由组件禁用并显示加载图标 |
| 图标与图标按钮 | `StellarIcon`、`StellarIconButton` |
| 文本输入、多选 | `StellarTextField`、`StellarCheckbox` |
| 搜索 | 统一使用 `StellarSearchField`，未传 `onClick` 时支持输入、清空和可选提交；传入时以相同外观显示只读关键词，整块点击返回编辑，不获取输入焦点。歌单页仅提交非空关键词后进入结果页，首页暂不执行搜索。顶部和左右使用 `spacing.page`，底部使用 `spacing.itemGap`。 |
| 歌单操作栏、创建弹窗 | `PlaylistToolbar` 右对齐加号按钮，`CreatePlaylistDialog` 输入名称与公开状态；提交、校验和网络状态由页面控制。 |
| 歌单可见性 | `PlaylistItem` 显示公开／私有状态；仅所有者的「更多」菜单提供切换操作，请求期间禁用菜单，失败用 Toast。 |
| 普通／内部／选中卡片 | `StellarCard(kind = Card / Inset / Selected)` |
| 表单分组 | `FormCard`，自带内容留白 |
| 确认、编辑、选择对话框 | `StellarAlertDialog` |
| 歌曲详情面板 | `StellarBottomSheet`，业务组合见 `TrackDetails` |
| 锚定小菜单 | `StellarActionMenu` + `StellarMenuAction`；歌单组合见 `PlaylistActionsMenu` |
| 歌曲／歌单列表 | `TrackItem`、`PlaylistItem` |
| 多选歌曲／选择歌单 | `TrackChoiceRow`、`PlaylistChoiceRow` |
| 加载、空内容、重试 | `StellarLoadingState`、`StellarEmptyState`、`StellarRetryAction` |
| 主界面骨架与底栏 | `StellarScaffold`、`StellarNavigationBar` |

底栏关闭波纹的实验性 Material 配置封装在 `StellarNavigationBar` 内；封面点击的无波纹行为封装在播放器组件内。页面无需知道它们使用哪个 Material API。

错误通过事件回调或 `LaunchedEffect` 调用 `ToastManager`。不创建与普通内容并列的错误文本，不在组合函数执行期间直接弹 Toast。

### 调用示例

```kotlin
FormCard {
    StellarTextField(value = name, onValueChange = onNameChange, label = "歌单名称")
    StellarButton(onClick = onSave, kind = StellarButtonKind.Secondary, loading = submitting) {
        StellarText("保存")
    }
}
```

```kotlin
StellarActionMenu(expanded, onDismiss, listOf(
    StellarMenuAction("重命名", R.drawable.ic_edit, onRename),
    StellarMenuAction("删除", R.drawable.ic_delete, onDelete, StellarMenuActionKind.Danger)
))
```

## 验证

- `powershell -NoProfile -File scripts/check-design-system.ps1`：检查组件边界、硬编码尺寸和页面样式泄漏，不修改文件。
- `gradlew.bat :app:compileDebugKotlin :app:lintAnalyzeDebug --offline`：编译和主应用 Lint 分析。
- `DesignSystemPreview.kt` 覆盖深浅主题、禁用、加载、危险操作、选择、长文本与放大字体；业务组件保留自身 Preview。
- 需要实际渲染检查文字对比度、弹窗尺寸、键盘避让和窄屏布局；编译通过不能替代视觉验证。
- 回归认证、上传、歌单操作及跨页播放；没有设备时应明确记录未做实机验证。
