# Composable 使用手册

本文对应当前项目源码，说明项目自定义的全部 Composable。颜色、圆角和间距的选择原则见 [设计规范](design-system.md)。不包含 Compose / Material 库本身的 API。

## 1. 阅读约定与快速选择

- 下列签名省略统一的 `@Composable` 注解；参数没有 `= 默认值` 时必须传入，即使类型可空。
- `modifier = Modifier` 表示默认不增加调用方布局修饰。通常用来设置宽度、外边距或权重，不用来绕过主题重新配置颜色、字体和圆角。内外 modifier 的顺序仍会影响测量结果。
- `content`、`text`、`confirmButton` 等是 Composable 内容插槽，不是普通字符串。`RowScope` / `ColumnScope` 插槽允许在对应布局中使用 `weight` 等作用域能力。
- 控件回调通常只通知事件，不替调用方更新状态、请求网络或切换页面。下文会明确标出包含业务行为的组件。
- 除特别说明外，时间进度参数单位都是 **毫秒**，不是秒，也不是 0～1 的比例。
- `public` 函数可作为项目调用入口；`internal` 仅模块内可见；`private` 是所在文件的实现细节，不应跨文件调用。
- 所有 UI 应处于 `StellarMusicTheme` 内。基础组件一般在 `ui/components`，页面在 `ui/screens`，播放器状态入口在 `ui/player`。
- 没有包在 `fun` 中的短示例应放入现有 Composable；示例中的 `player`、`track`、`submitting` 和事件回调来自所在页面。第10节提供带参数声明的完整组合示例。

| 想实现的内容 | 优先使用 |
| --- | --- |
| 普通文字、按钮、输入框 | `StellarText`、`StellarButton`、`StellarTextField` |
| 搜索输入与点击返回 | `StellarSearchField` |
| 歌单操作栏与创建 | `PlaylistToolbar`、`CreatePlaylistDialog` |
| 图标、图标按钮、多选 | `StellarIcon`、`StellarIconButton`、`StellarCheckbox` |
| 分隔线、加载指示器 | `StellarDivider`、`StellarLoadingIndicator` |
| 卡片、带内边距的表单 | `StellarCard`、`FormCard` |
| 确认或编辑弹窗 | `StellarAlertDialog` |
| 更多菜单、底部面板 | `StellarActionMenu`、`StellarBottomSheet` |
| 页面骨架、底部导航 | `StellarScaffold`、`StellarNavigationBar` |
| 歌曲、歌单及选择行 | `TrackItem`、`PlaylistItem`、`TrackChoiceRow`、`PlaylistChoiceRow` |
| 歌曲详情、加入歌单 | `TrackDetails`、`AddToPlaylistDialog`、`AddTracksDialog` |
| 播放器 | `PlayerBar`；`PlayerBarContent` 是内部渲染入口 |

## 2. 基础控件

源码：[StellarControls.kt](../app/src/main/java/top/stellortus/stellar_music/ui/components/StellarControls.kt)。

### 2.1 StellarText

显示普通文字；通过语义角色选择字体，通过语义色调选择前景色。

```kotlin
fun StellarText(
    text: String,
    modifier: Modifier = Modifier,
    role: StellarTextRole = StellarTextRole.Inherit,
    tone: StellarTone = StellarTone.Inherit,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
    softWrap: Boolean = true
)
```

| 参数 | 怎么设置 |
| --- | --- |
| `text` | 要显示的字符串。没有富文本或资源 ID 重载。 |
| `role` | 见下表；按钮内通常保持 `Inherit`，普通正文可选 `Body`。 |
| `tone` | 见色调表；按钮内保持 `Inherit`，辅助信息常用 `Secondary`。 |
| `maxLines` | 正整数；列表名称常设为 `1`。默认不限制。 |
| `overflow` | 默认裁剪。需要省略号时同时设置 `maxLines` 与 `TextOverflow.Ellipsis`。 |
| `textAlign` | `null` 使用当前文字样式的对齐方式；可传 `TextAlign.Center`、`End` 等。 |
| `softWrap` | `true` 允许换行；`false` 不按可用宽度自动折行，但不会自动滚动。 |
| `modifier` | 通用布局参数；例如 `fillMaxWidth()` 后搭配居中对齐。 |

`StellarTextRole` 的全部可选值：

| 值 | 用途／Theme 字段 |
| --- | --- |
| `Inherit` | 继承当前容器的文字样式 `LocalTextStyle`。 |
| `Title` | 详情名称，`typography.title`；不用于重新添加页面大标题。 |
| `ItemTitle` | 歌曲、歌单条目名称，`typography.itemTitle`。 |
| `Body` | 常规正文，`typography.body`。 |
| `BodySmall` | 较小正文，`typography.bodySmall`。 |
| `Action` | 操作文字及紧凑播放器名称，`typography.action`。 |
| `Caption` | 数量、时间、版本等，`typography.caption`。 |
| `Micro` | 播放器辅助信息，`typography.micro`。 |
| `Avatar` | 头像首字，`typography.avatar`。 |

`StellarTone` 同时供文字、图标及加载指示器使用：

| 值 | 对应颜色／使用场景 |
| --- | --- |
| `Inherit` | 继承容器前景；填充按钮内优先使用。 |
| `Primary` | `textPrimary`，中性底色上的主要文字。 |
| `Secondary` | `textSecondary`，辅助信息。 |
| `Accent` | `accent`，强调操作、链接、选中导航图标。 |
| `Danger` | `dangerForeground`，中性底色上的危险操作。 |
| `OnPrimaryAction` | `onPrimaryAction`，配 `primaryAction` 背景。 |
| `OnSecondaryAction` | `onSecondaryAction`，配 `secondaryAction` 背景。 |
| `OnSelected` | `onSelectedBackground`，配 `selectedBackground`。 |
| `OnDanger` | `onDangerBackground`，配 `dangerBackground`。 |
| `Cover` | `coverForeground`，占位封面音符。 |

```kotlin
StellarText("歌曲名称", role = StellarTextRole.ItemTitle,
    maxLines = 1, overflow = TextOverflow.Ellipsis)
StellarText("未知歌手", role = StellarTextRole.BodySmall, tone = StellarTone.Secondary)
```

长歌名往返滚动由播放器内部 `PlayerTrackTitle` 完成，`StellarText` 自身没有滚动能力。

### 2.2 StellarButton

统一操作按钮的形状、颜色、加载和禁用状态。

```kotlin
fun StellarButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: StellarButtonKind = StellarButtonKind.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    content: @Composable RowScope.() -> Unit
)
```

| 参数 | 怎么设置 |
| --- | --- |
| `onClick` | 普通点击回调；需要异步操作时由调用方启动协程。 |
| `kind` | `Primary` 主操作；`Secondary` 次要填充操作；`Outlined` 描边；`Text` 取消、返回等轻操作；`Danger` 删除等危险填充操作。 |
| `enabled` | 是否允许点击；默认 `true`。 |
| `loading` | 默认 `false`。为 `true` 时在内容前显示加载图标并禁用按钮。实际可点击条件是 `enabled && !loading`。 |
| `content` | 横向内容插槽，通常为文字或图标加文字；按钮不会自动更换文字。 |
| `modifier` | 通用布局参数；整行主操作可设置 `fillMaxWidth()`。 |

不在组件内部持有 `loading`，也不会自动结束请求。即使 `onClick` 抛出异常，组件也不会替调用方捕获或提示错误。

```kotlin
// submitting 和 onSave 由页面持有。
StellarButton(onClick = onSave, kind = StellarButtonKind.Secondary,
    loading = submitting, modifier = Modifier.fillMaxWidth()) {
    StellarText(if (submitting) "保存中…" else "保存")
}
```

按钮内容保持继承色调，不要给 `Danger` 按钮内文字再设置 `tone = Danger`，否则会破坏前景／背景配对。按钮尺寸、边框和禁用颜色在 Theme 与组件实现中统一控制。

### 2.3 StellarIcon

```kotlin
fun StellarIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tone: StellarTone = StellarTone.Inherit
)
```

| 参数 | 怎么设置 |
| --- | --- |
| `painter` | 传入 `painterResource(R.drawable.ic_play)` 等 Painter。 |
| `contentDescription` | 必填的可空参数；独立操作图标使用中文说明，与旁边文字重复的装饰图标可传 `null`。 |
| `tone` | 使用上文的 `StellarTone`；默认继承父容器。 |
| `modifier` | 可通过 Theme 尺寸设置显示大小，例如 `.size(StellarTheme.sizes.menuIcon)`。 |

只绘制图标，不响应点击。

```kotlin
StellarIcon(painterResource(R.drawable.ic_play), contentDescription = "播放")
```

### 2.4 StellarIconButton

```kotlin
fun StellarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
)
```

`onClick` 是操作回调，`enabled = false` 禁用操作；`content` 通常传入一个 `StellarIcon`。组件追加 Theme 中的最小操作尺寸 `sizes.touch`，当前为 48dp；父布局约束仍可能限制最终尺寸。此组件不统一关闭波纹，底栏与封面各自的无波纹行为由对应业务组件处理。

```kotlin
StellarIconButton(onClick = onPlay) {
    StellarIcon(painterResource(R.drawable.ic_play), "播放")
}
```

### 2.5 StellarTextField

```kotlin
fun StellarTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
)
```

| 参数 | 怎么设置 |
| --- | --- |
| `value` | 当前内容，由调用方维护。 |
| `onValueChange` | 收到新内容后更新 `value`；组件不自己保存输入。 |
| `label` | 输入框标签字符串，例如「用户名」。 |
| `enabled` | 默认允许编辑；提交期间可设为 `false`。 |
| `singleLine` | 默认单行；设为 `false` 允许多行输入。当前未暴露 `maxLines`。 |
| `visualTransformation` | 默认原样显示；密码使用 `PasswordVisualTransformation()`。仅改变显示，不加密状态中的字符串。 |
| `keyboardOptions` | 配置键盘类型等，例如 `KeyboardOptions(keyboardType = KeyboardType.Password)`。 |
| `modifier` | 表单中通常使用 `fillMaxWidth()`。 |

当前不提供内联错误文案或 `isError` 参数。校验由业务层执行，失败通过 Toast 提示。

```kotlin
var password by remember { mutableStateOf("") }
StellarTextField(password, { password = it }, "密码", Modifier.fillMaxWidth(),
    visualTransformation = PasswordVisualTransformation(),
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
```

### 2.6 StellarCheckbox

```kotlin
fun StellarCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)
```

`checked` 是外部状态；非空 `onCheckedChange` 接收期望的新状态，调用方需要更新 `checked`。传 `null` 时复选框自身不处理切换，适合由整行 `toggleable` 负责点击的场景；不是「不传回调」，因为该参数没有默认值。`enabled = false` 同时禁用交互并显示禁用样式。

```kotlin
var checked by remember { mutableStateOf(false) }
StellarCheckbox(checked, onCheckedChange = { checked = it })
```

### 2.7 StellarDivider

```kotlin
fun StellarDivider(modifier: Modifier = Modifier)
```

水平分隔线，粗细使用 `sizes.hairline`，颜色使用 `colors.divider`。默认横向填满可用宽度。歌曲列表使用 `TrackItem(showDivider = ...)`，不要再额外添加一条线。

```kotlin
StellarDivider()
```

### 2.8 StellarLoadingIndicator

```kotlin
fun StellarLoadingIndicator(
    modifier: Modifier = Modifier,
    size: StellarIndicatorSize = StellarIndicatorSize.Regular,
    tone: StellarTone = StellarTone.Accent
)
```

`Regular` 使用 `sizes.loadingIndicator` 与 `sizes.progress`，当前直径40dp、描边4dp；`Small` 和 `Button` 当前都使用直径18dp、描边2dp，分别表达列表和按钮场景。`tone` 决定颜色，默认强调色。组件只是不确定进度的圆形动画，不接受百分比，也不自动请求数据。

```kotlin
StellarLoadingIndicator(size = StellarIndicatorSize.Small)
```

### 2.9 StellarSearchField

```kotlin
fun StellarSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索",
    enabled: Boolean = true,
    onSearch: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null
)
```

单行搜索输入框，宽度填满可用空间。调用方保存 `value`，通过 `onValueChange` 更新；组件不保存文本、不请求数据、不筛选列表。有内容时显示清空按钮，点击回调空字符串；键盘搜索键清除焦点、收起键盘，并通过可选 `onSearch` 回调提交当前原始文本，调用方负责 trim 和校验。`placeholder` 设置空值提示，`enabled` 控制输入和清空操作。颜色、字体、形状均来自 Theme。

首页使用“搜索音乐”，暂不执行搜索；歌单列表页使用“搜索歌单”，输入不改变列表，提交非空关键词后导航到服务端搜索结果页。页面在列表外固定放置搜索框。

`onClick` 默认为空，此时正常输入、清空并通过键盘提交。非空时同一个输入框切换为只读点击模式，整块触发回调，不获取焦点、不弹键盘、不显示清空按钮；颜色保持正常启用状态，不使用禁用态模拟。`contentDescription` 配置点击模式的无障碍说明，默认使用当前值或占位文案。结果页使用 `value` 显示关键词，并配置点击返回歌单页。

### 2.10 PlaylistToolbar、CreatePlaylistDialog

```kotlin
fun PlaylistToolbar(onCreate: () -> Unit, enabled: Boolean = true)
fun CreatePlaylistDialog(
    name: String, onNameChange: (String) -> Unit,
    isPublic: Boolean, onPublicChange: (Boolean) -> Unit,
    submitting: Boolean, onCreate: () -> Unit, onDismiss: () -> Unit
)
```


`PlaylistToolbar` 是全宽、右对齐的加号操作栏，使用页面水平间距；`onCreate` 通知创建操作，`enabled` 控制按钮。

`CreatePlaylistDialog` 由调用方保存名称与公开状态，显示名称输入、公开复选框和说明。`submitting` 为真时禁止修改、关闭和重复提交，并显示按钮加载状态；空白名称禁用创建按钮。调用方负责 trim、请求和 Toast，成功后移除弹窗。歌单页每次打开默认空名称、私有。

## 3. 容器、浮层与导航

### 3.1 StellarCard

```kotlin
fun StellarCard(
    modifier: Modifier = Modifier,
    kind: StellarSurfaceKind = StellarSurfaceKind.Card,
    content: @Composable () -> Unit
)
```

`modifier` 控制外部布局；`content` 是通用内容插槽，**组件不自带内容内边距、不自动排列内容，也不自动处理点击**。

| `kind` | 用途 | 背景／前景 | 圆角 |
| --- | --- | --- | --- |
| `Card` | 普通卡片 | cardBackground / textPrimary | card |
| `Inset` | 内部分组 | insetBackground / textPrimary | item |
| `Selected` | 选中条目 | selectedBackground / onSelectedBackground | item |
| `Item` | 普通歌曲行 | itemBackground / textPrimary | item |
| `Player` | 播放器 | playerBackground / textPrimary，带 cardElevation | card |
| `Avatar` | 头像 | selectedBackground / onSelectedBackground | avatar |

```kotlin
StellarCard(kind = StellarSurfaceKind.Inset) {
    StellarText("歌曲信息", Modifier.padding(StellarTheme.spacing.section))
}
```

### 3.2 FormCard

```kotlin
fun FormCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
)
```

带统一内边距的表单卡片，内部是纵向布局。卡片填满可用宽度，内边距为 `spacing.page`，子项间距为 `spacing.section`。与 `StellarCard` 不同，它已经提供内容留白，不必为每个输入框重复加内边距。`modifier` 设置卡片外部位置与边距。

```kotlin
FormCard {
    StellarText("账号信息", role = StellarTextRole.ItemTitle)
    StellarText("登录后可管理歌单", tone = StellarTone.Secondary)
}
```

### 3.3 StellarAlertDialog

```kotlin
fun StellarAlertDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    text: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null
)
```

| 参数 | 怎么设置 |
| --- | --- |
| `title` | 简短的操作说明，例如「删除歌单」。使用 Theme 的渐变弹窗标题样式。 |
| `onDismissRequest` | 点击外部或按返回时发出的关闭请求。组件不持有显示状态；调用方决定是否关闭，也可在提交中忽略此请求。 |
| `confirmButton` | 必填内容插槽，一般放一个 `StellarButton`。只列出可选内容的弹窗可传空 `{}`。 |
| `text` | 必填正文插槽，可放普通文字、输入框或受限高度的列表。 |
| `dismissButton` | 默认 `null`，不展示取消按钮；需要时传文字按钮。 |

点击确认或取消插槽里的按钮不会自动触发 `onDismissRequest`，必须在按钮回调中更新外部状态。背景、圆角和文字颜色由 Theme 统一提供。

### 3.4 StellarActionMenu 与 StellarMenuAction

```kotlin
fun StellarActionMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    actions: List<StellarMenuAction>
)

data class StellarMenuAction(
    val label: String,
    val iconRes: Int,
    val onClick: () -> Unit,
    val kind: StellarMenuActionKind = StellarMenuActionKind.Normal
)
```

| 参数 | 怎么设置 |
| --- | --- |
| `expanded` | 外部控制显示，默认值不存在。 |
| `onDismiss` | 关闭请求，通常设为 `{ expanded = false }`。 |
| `actions` | 按显示顺序传入操作项；建议少量操作，当前无逐项 `enabled` 参数。 |
| `label` | 操作文字。 |
| `iconRes` | drawable 资源 ID，例如 `R.drawable.ic_edit`；不是 Painter。 |
| `onClick` | 该项动作回调。点击时先执行菜单的 `onDismiss`，再执行此回调。 |
| `kind` | `Normal` 使用次要填充按钮；`Danger` 使用危险操作配色。 |

它是锚定弹出菜单，应与触发按钮放在同一个 `Box` 等锚定布局内，而不是单独放在整个页面外层。它不会自己把 `expanded` 改为 `false`，只有调用方更新后才会关闭。

### 3.5 PlaylistActionsMenu

```kotlin
fun PlaylistActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    isPublic: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null
)
```

`StellarActionMenu` 的歌单专用组合：包含「重命名」和「删除」。前四个参数必填；`expanded`、`onDismiss` 与通用菜单相同。`onToggleVisibility` 非空时增加切换入口，`isPublic` 决定显示「设为私有」还是「设为公开」。所有回调只通知操作，不直接请求接口。

### 3.6 StellarBottomSheet

```kotlin
fun StellarBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
)
```

统一底部面板的背景、圆角、前景和遮罩。`onDismissRequest` 通知外部关闭，`content` 提供面板内容。它没有 `expanded` 参数：调用方通过是否将该 Composable 放入组合来控制显示。

组件未额外添加业务内容的内边距、滚动和底部安全区；内容较长时由组合组件安排，参考 `TrackDetails`。它也不提供播放器或网络状态。

```kotlin
if (showSheet) {
    StellarBottomSheet(onDismissRequest = { showSheet = false }) {
        StellarText("详细信息", Modifier.padding(StellarTheme.spacing.page))
    }
}
```

### 3.7 StellarScaffold

```kotlin
fun StellarScaffold(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
)
```

页面骨架，使用 Theme 页面底色。`bottomBar` 必填，不需要底栏时传 `{}`；`content` 接收避让系统区域和底栏的 `PaddingValues`，**调用方必须应用它**，否则内容可能被底栏覆盖。组件不管理当前页面或导航状态。

```kotlin
StellarScaffold(bottomBar = {}) { innerPadding ->
    Column(Modifier.fillMaxSize().padding(innerPadding)) {
        StellarText("正文", Modifier.padding(StellarTheme.spacing.page))
    }
}
```

### 3.8 StellarNavigationBar

```kotlin
fun StellarNavigationBar(selected: BottomTab, onSelect: (BottomTab) -> Unit)
```

`selected` 是当前菜单项；`onSelect` 返回用户点击的项，调用方负责改变状态或导航。支持的 `BottomTab` 固定为 `HomeTab`、`PlayListTab`、`UploadTab`、`AccountTab`，按枚举顺序展示「首页、歌单、上传、我的」。

组件内部关闭按动波纹并统一选中样式，但不会创建 `NavController`，也没有左右滑动或底栏拖动切换功能。相同项再次点击仍可能通知 `onSelect`，调用方可自行判断。

## 4. 状态展示

源码：[StellarStates.kt](../app/src/main/java/top/stellortus/stellar_music/ui/components/StellarStates.kt)。

| Composable 签名 | 用途与参数 |
| --- | --- |
| `StellarLoadingState(message: String, modifier: Modifier = Modifier)` | 填满可用区域并居中显示转圈与说明；`message` 为必填提示，不发起请求。外层须提供有限高度，不要直接放进无限高度的滚动内容。 |
| `StellarEmptyState(message: String, modifier: Modifier = Modifier)` | 横向填满、带 section 内边距的居中空内容提示，不主动填满整个高度。`message` 为正常空状态，不是错误信息。 |
| `StellarRetryAction(onRetry: () -> Unit, modifier: Modifier = Modifier)` | 显示固定文字「重试」的描边按钮，点击通知 `onRetry`；不会自动请求、维护 loading 或显示错误。 |

```kotlin
StellarEmptyState("还没有歌单")
StellarRetryAction(onRetry = onReload)
```

项目不提供「内联错误状态」组件。请求失败通过回调或 `LaunchedEffect` 调用 `ToastManager`；正文可以保留重试操作。

## 5. 业务展示组件

### 5.1 CoverPlaceholder

```kotlin
fun CoverPlaceholder(size: CoverPlaceholderSize, modifier: Modifier = Modifier)
```

绘制带渐变背景和居中音符的封面占位，既可用于歌曲，也可用于歌单。没有真实图片加载、点击或旋转功能。

| `size` | 外框／音符的当前尺寸 | 常见位置 |
| --- | --- | --- |
| `Small` | 40dp / 22dp | 播放器、紧凑选择行 |
| `Medium` | 52dp / 28dp | 歌曲、歌单列表 |
| `Large` | 96dp / 52dp | 详情和上传页 |

`size` 必填，调用处选择枚举；具体值由 Theme 决定。`modifier` 可用于布局或动画，但不要通过任意外部尺寸取代枚举配置。播放器中的旋转和点击是外层组件增加的。

```kotlin
CoverPlaceholder(CoverPlaceholderSize.Medium)
```

### 5.2 TrackItem

```kotlin
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isCurrent: Boolean = false,
    showDivider: Boolean = false
)
```

| 参数 | 怎么设置 |
| --- | --- |
| `track` | common DTO；显示歌名、歌手及占位封面，歌手为空显示「未知歌手」。 |
| `onClick` | 必填点击回调；通常调用播放器播放这首歌。条目自身不会播放。 |
| `onLongClick` | 默认 `null`；传入后可在长按时打开 `TrackDetails`。 |
| `isCurrent` | 当前歌曲时设为 `true`，只改变选中背景；不会显示「播放中／已暂停」文字。 |
| `showDivider` | 是否显示条目全宽的底部分隔线；列表末项应设为 `false`。 |
| `modifier` | 外部边距等，首页通常使用 `spacing.homeTrackMargin`。 |

内部已经设置紧凑的图文间距和纵向留白，列表不需要再叠加歌曲之间的空白。

```kotlin
TrackItem(track, onClick = { player.play(track) },
    onLongClick = { selectedTrack = track },
    isCurrent = player.currentTrack?.id == track.id,
    showDivider = index < tracks.lastIndex)
```

### 5.3 PlaylistItem

```kotlin
fun PlaylistItem(
    playlist: Playlist,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onToggleVisibility: (() -> Unit)? = null,
    actionsEnabled: Boolean = true,
    showActions: Boolean = true
)
```

前四个参数必填。`playlist` 提供名称、公开／私有状态、歌曲数量和更新时间；`onOpen` 在点击卡片时触发；`onRename`、`onDelete` 分别在「更多」菜单选择相应项后触发。额外可选参数 `onToggleVisibility: (() -> Unit)? = null` 非空时显示可见性操作；页面仅为所有者传入此回调。`actionsEnabled: Boolean = true` 控制「更多」按钮和菜单，请求时禁用。组件自己维护菜单显示状态，不请求接口。

组件自带全宽布局与上下8dp外边距，没有 `modifier` 参数；需要额外布局时由外层容器处理。

`showActions = false` 隐藏整个「更多」区域，用于搜索结果；点击条目仍触发 `onOpen`。

```kotlin
PlaylistItem(playlist, onOpen = onOpen,
    onRename = { renameTarget = playlist }, onDelete = { deleteTarget = playlist })
```

### 5.4 TrackChoiceRow

```kotlin
fun TrackChoiceRow(
    track: Track,
    selected: Boolean,
    alreadyAdded: Boolean,
    onToggle: () -> Unit
)
```

多选歌曲行，四个参数均必填。`selected` 控制复选框及选中背景；`alreadyAdded = true` 禁用整行并显示「已在歌单中」。两者相互独立，调用方应对已添加的歌曲传 `selected = true`。`onToggle` 不带新值，由调用方根据已有选择集合切换。

内部复选框回调为 `null`，由整行切换选择，避免同一次点击修改两次状态。不请求接口。

```kotlin
TrackChoiceRow(track, selected = track.id in selectedIds,
    alreadyAdded = false) {
    selectedIds = if (track.id in selectedIds) selectedIds - track.id else selectedIds + track.id
}
```

### 5.5 PlaylistChoiceRow

```kotlin
fun PlaylistChoiceRow(playlist: Playlist, enabled: Boolean, onClick: () -> Unit)
```

用于选择目标歌单，显示小封面、名称和数量。三个参数均必填；`enabled = false` 禁止点击，常用于提交期间；`onClick` 通知选择，不会自行添加歌曲或关闭弹窗。

```kotlin
PlaylistChoiceRow(playlist, enabled = !submitting, onClick = onChoose)
```

### 5.6 ProfileIdentity

```kotlin
fun ProfileIdentity(username: String, userId: Int?)
```

账号信息卡片。`username` 提供头像首字与名称；空白名称显示「音乐爱好者」，空白头像使用音符。`userId` 虽可空但必须显式传入：`null` 隐藏 ID，其余值原样显示，不自动校验是否有效。

只展示参数，不读取账号偏好、不请求用户资料。

```kotlin
ProfileIdentity(username = "Stellar", userId = null)
```

### 5.7 TrackDetails

```kotlin
fun TrackDetails(track: Track, onDismiss: () -> Unit)
```

歌曲详情业务组合，供长按歌曲时使用。显示歌名、歌手、歌曲 ID、非空上传者／文件名，以及有效上传时间。`track` 为当前数据，不会为补字段额外请求详情。

组件自己维护「详情面板／选择歌单弹窗」两阶段状态。点击「加入歌单」后移除面板并显示 `AddToPlaylistDialog`，后者会请求歌单列表。外部通过是否调用此函数控制整个流程，`onDismiss` 应清除选中歌曲；关闭选歌单弹窗后结束整个流程，不返回详情面板。

```kotlin
selectedTrack?.let { track ->
    TrackDetails(track, onDismiss = { selectedTrack = null })
}
```

## 6. 播放器

### 6.1 PlayerBar：页面接入入口

```kotlin
fun PlayerBar(state: MusicPlayerState, modifier: Modifier = Modifier)
```

接收外部持有的 `MusicPlayerState`。`currentTrack == null` 时不显示；有歌曲时将播放状态、进度与回调交给 `PlayerBarContent`。组件不创建或释放播放器，调用方应在稳定的页面宿主中创建状态，并在退出时释放，参考后面的完整接入示例。

`modifier` 作用于播放器外层布局。该入口使用 Media3 标记，调用处可能需要 `@androidx.annotation.OptIn(UnstableApi::class)`。

### 6.2 PlayerBarContent：internal 渲染组件

```kotlin
internal fun PlayerBarContent(
    track: Track,
    isPlaying: Boolean,
    positionMs: Float,
    durationMs: Float,
    onTogglePause: () -> Unit,
    onSeekPreview: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    modifier: Modifier = Modifier
)
```

| 参数 | 怎么设置 |
| --- | --- |
| `track` | 当前歌曲；更换 ID 时封面旋转角度重新开始。 |
| `isPlaying` | 决定按钮图标、无障碍说明和封面是否旋转。暂停时保留角度，继续后接着转。 |
| `positionMs` | 显示的进度，毫秒 `Float`；拖动期间可传预览进度。 |
| `durationMs` | 总时长，毫秒 `Float`；未知可传 `0f`，不会自动推断时长。 |
| `onTogglePause` | 播放按钮和封面点击共用回调；封面点击无波纹。 |
| `onSeekPreview` | 点击或拖动轨道时连续传出期望毫秒值；一般只更新预览状态。 |
| `onSeekFinished` | 点击定位完成或拖动结束时通知提交 seek；当前实现拖动取消时也会触发。 |
| `modifier` | 播放器卡片外层布局。 |

它不操作 Media3。通常使用 `PlayerBar`，仅在模块内部需要纯展示或预览时直接调用此函数。

### 6.3 PlayerTrackTitle：internal 歌名动画

```kotlin
internal fun PlayerTrackTitle(title: String, trackId: Int)
```

`title` 为完整歌名；`trackId` 用来区分歌曲并重置动画。组件测量文本与可用宽度，仅超长时往返移动，端点停顿由 Theme 配置，不复制文字或首尾循环。宽度改变后会重新计算并从开头开始。

没有 `isPlaying` 参数，因此只要仍在组合中，长歌名即会滚动，暂停歌曲不会单独停止文字动画。应置于有界宽度的容器中，例如播放器内部的加权 `Column`，不在无限横向空间中使用。

## 7. 页面与业务弹窗入口

这些函数通常包含接口请求、账号读写或页面导航，不是通用基础控件。没有声明默认值的参数均必填。

歌单导航与登录／注册导航显式关闭进入、退出及返回时的过渡动画，直接切换页面；不改变弹窗、菜单和播放器动画。

歌单列表与搜索结果路由还分别显式关闭进入、退出、返回和尺寸过渡，避免后续导航容器配置改变这两个页面的直接切换行为。

| 签名 | 用途、参数与行为 |
| --- | --- |
| `HomeScreen(player: MusicPlayerState)` | 音乐首页，顶部固定搜索框仅支持输入和清空。`player` 为共享播放器；进入时请求音乐列表，滚到底部加载更多。点击歌曲调用播放，长按显示详情。错误通过 Toast，重试由页面状态驱动。 |
| `PlayListScreen(player: MusicPlayerState)` | 歌单功能的导航容器。`player` 传给详情页；内部创建 `NavController`，管理歌单列表和详情路由。注意名称是 `PlayListScreen`。 |
| `PlaylistListScreen(pageState: PlaylistListState, listState: LazyListState, onOpenPlaylist: (Int) -> Unit, onSearch: (String) -> Unit)` | 导航层保存 `pageState` 中的列表与输入，以及 `listState` 滚动位置。输入和清空不请求、不导航；键盘搜索键提交 trim 后的非空关键词，通过 `onSearch` 导航。下方加号创建歌单，支持重命名、删除及所有者切换可见性。 |
| `PlaylistSearchScreen(state: PlaylistSearchState, listState: LazyListState, onBack: () -> Unit, onOpenPlaylist: (Int) -> Unit)` | 独立结果页。首次进入对已提交关键词调用 `GET /playlists/search?key=...`，返回 `List<Playlist>`。顶部关键词区域点击返回编辑，无独立返回按钮或输入焦点。返回歌单页取消未完成请求；失败 Toast 加重试，结果仅支持进入详情。导航层保存结果及滚动位置，从详情返回不重复搜索。 |
| `PlaylistDetailScreen(playlistId: Int, player: MusicPlayerState, onBack: () -> Unit)` | 按 `playlistId` 请求详情；`player` 用于播放；`onBack` 通知返回。内部处理编辑、添加歌曲及刷新。Media3 使用标记与播放器入口相同。 |
| `UploadScreen()` | 无参数。调用系统音频选择器，选择文件后自动发起上传，并维护上传中／成功状态；失败使用 Toast。临时文件在请求结束后清理。 |
| `AccountScreen(onLoggedIn: () -> Unit)` | 登录／注册导航容器；内部管理两页路由，外层统一消费安全区与键盘 Insets，表单和版本号不重复预留键盘高度。表单顶部对齐，小屏时可滚动；版本号随键盘上移。认证成功并保存凭据后调用 `onLoggedIn`，由上层切到主界面。 |
| `ProfileScreen(onLogout: () -> Unit)` | 从账号偏好读取名称和 ID；点击退出后请求注销，成功清理账号并调用 `onLogout`。失败仅 Toast，不调用成功回调。 |
| `AddToPlaylistDialog(track: Track, onDismiss: () -> Unit)` | 进入时请求歌单列表；`track` 是待添加歌曲。点击歌单即请求添加，成功调用 `onDismiss` 并提示成功；失败保留弹窗并 Toast。提交中屏蔽取消／外部关闭及重复选择。 |
| `AddTracksDialog(existingTrackIds: Set<Int>, onDismiss: () -> Unit, onConfirm: (List<Int>) -> Unit)` | 进入时按现有 API 加载曲库（当前内部最多500首），按 ID 去重。`existingTrackIds` 指定已在歌单中的歌曲，勾选并禁用；`onConfirm` 只返回本次新增选择的 ID，不发起添加请求，也不自动关闭。`onDismiss` 通知取消。没有选择时不能确认。 |

`AddTracksDialog` 自己持有勾选集合，调用方负责提交结果、成功后关闭与刷新。它目前没有 `submitting` 参数，不应在文档或调用处假定其确认按钮会因外部请求自动禁用。

```kotlin
if (showAddTracks) {
    AddTracksDialog(
        existingTrackIds = existingIds,
        onDismiss = { showAddTracks = false },
        onConfirm = onSubmitTrackIds
    )
}
```

## 8. 主题入口与样式读取

### 8.1 StellarMusicTheme

```kotlin
fun StellarMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
)
```

`darkTheme` 默认跟随系统，也可在预览中明确传 `true` 或 `false`。`dynamicColor = true` 且系统为 Android 12及以上时使用系统动态配色，否则使用项目的深浅配色；默认关闭以保持品牌颜色。`content` 必填。

它安装 Material 字体、圆角与颜色，并创建填满可用区域的页面底色 `Surface`，因此用于应用根部或独立 Preview，不应在每个列表条目里重复包裹。

```kotlin
StellarMusicTheme(darkTheme = true) {
    StellarText("深色主题示例", role = StellarTextRole.Body)
}
```

### 8.2 Theme 中的 Composable getter

下列是读取当前组合环境的属性，不是绘制 UI 的组件；都没有函数参数：

| 属性 | 用途 |
| --- | --- |
| `StellarTheme.colors` | 当前语义颜色与渐变。只在组件层使用，不让页面直接挑颜色。 |
| `StellarTheme.shapes` | 卡片、面板、输入框、封面等语义圆角。 |
| `StellarTheme.typography` | 语义文字样式；页面使用 `StellarTextRole`。 |
| `StellarTheme.spacing` | 页面、卡片和条目间距；页面布局可以读取。 |
| `StellarTheme.sizes` | 封面、触控、进度、菜单等尺寸；页面布局可以读取。 |
| `StellarTheme.motion` | 封面旋转速度、歌名滚动速度与端点停顿。 |
| `StellarTheme.dialogTitleStyle` | `dialogTitleStyle` 扩展属性，将详情标题字号与强调渐变组合；由弹窗内部使用，调用时需导入扩展。 |

字段用途与数值以 [设计规范](design-system.md) 和 `ui/theme` 为准。参数中没有暴露颜色或圆角时，不要给组件临时新增随意覆盖样式的参数。

## 9. 内部实现附录

### 9.1 私有／内部辅助 Composable

下面逐项记录用途和参数，便于读源码；除标为 `internal` 的函数外均为 `private`，不是页面的可复用 API。

| 函数及签名 | 用途与参数 |
| --- | --- |
| `private fun StellarTone.color(): Color` | 将接收者枚举转换为当前 Theme 的颜色；`Inherit` 读取 `LocalContentColor`。无显式参数、不绘制 UI，仅基础控件内部调用。 |
| `internal fun StellarMenuContent(actions: List<StellarMenuAction>, onDismiss: () -> Unit = {})` | 菜单按钮的纯内容，不创建 Popup。`actions` 与通用菜单相同；每项先调用 `onDismiss` 再执行动作。默认关闭回调为空，供预览独立使用。 |
| `private fun SeekBar(value: Float, durationMs: Float, onValueChange: (Float) -> Unit, onValueChangeFinished: () -> Unit, modifier: Modifier = Modifier)` | 播放器轨道绘制与手势；`value`、`durationMs` 为毫秒；`onValueChange` 输出预览毫秒；`onValueChangeFinished` 在提交或取消拖动时触发。布局参数通常提供全宽和 `seekHeight`。内部将可用最大值至少视为1以避免除零，比例限制在0～1；调用方仍应提供合理进度。 |
| `private fun TrackList(tracks: List<Track>, isLoadingMore: Boolean, endReached: Boolean, onLoadMore: () -> Unit, modifier: Modifier = Modifier, currentTrackId: Int?, onTrackClick: (Track) -> Unit, onTrackLongClick: (Track) -> Unit)` | 首页列表组合。`tracks` 为已加载项；两个布尔值控制底部提示；末项可见时通知 `onLoadMore`，调用方防止并发和越界。`currentTrackId = null` 表示无高亮；两个歌曲回调分别通知点击与长按。除 modifier 外均必填。 |
| `private fun LoadingMoreFooter()` | 无参数；显示小加载指示器与「正在加载更多…」。不触发请求。 |
| `private fun EndReachedFooter()` | 无参数；显示「已经到底啦」，不决定是否真的到底。 |
| `internal fun VersionFooter(modifier: Modifier = Modifier)` | 从 `PackageManager` 读取并记住当前版本，显示版本文案与 Theme 竖直间距。安全区由调用方通过 `modifier` 或父布局处理；账号导航外层统一处理，个人页显式传入安全区修饰符。不是通用静态文字组件。 |
| `private fun LoginScreen(onLoggedIn: () -> Unit, onNavigateToRegister: () -> Unit)` | 持有登录输入及 loading，执行校验和认证，保存账号后调用 `onLoggedIn`；`onNavigateToRegister` 通知切到注册页。两个回调均必填。 |
| `private fun RegisterScreen(onLoggedIn: () -> Unit, onNavigateToLogin: () -> Unit)` | 持有注册及确认密码状态；成功后保存账号并调用 `onLoggedIn`，另一回调通知返回登录。 |
| `private fun MusicApp()` | 无参数；应用认证调度根部，检查凭据并请求账号信息，决定显示登录、主界面、加载或重试入口。 |
| `private fun MainScreen(onLogout: () -> Unit)` | `onLogout` 通知回到未认证流程。持有底栏选择和共享播放器，退出组合时释放播放器；统一消费播放错误并 Toast。 |
| `private fun LoadingScreen()` | 无参数；认证检查期间展示带安全区的 `StellarLoadingState`。 |
| `private fun NetworkErrorScreen(onRetry: () -> Unit, onGoToLogin: () -> Unit)` | 两个必填回调分别重试认证检查、进入登录；不展示内联错误文本，也不自己发起网络请求。 |

### 9.2 Preview Composable

以下全部为 `private fun 名称()`，**没有参数**，仅用于 Android Studio Preview；不应在正式页面调用。多个 `@Preview` 注解是同一函数的不同配置，不是多个组件。

| 函数 | 用途 |
| --- | --- |
| `ComponentsPreview()` | 基础组件目录：按钮种类、禁用和 loading、输入框、复选框、封面、菜单、当前歌曲、空内容、重试；覆盖深浅主题和放大字体。 |
| `DialogPreview()` | 表单式编辑弹窗的深浅主题、窄屏与放大字体。 |
| `PlayerPreview()` | 播放器布局、长歌名、进度与播放状态的样例，不创建真实播放器。 |
| `PlaylistPreview()` | 歌单卡片的长名称及不同主题、字体尺寸。 |
| `CreatePlaylistPreview()` | 创建歌单弹窗的浅色、深色和大字体布局。 |
| `SearchResultsPreview()` | 无返回按钮的结果页顶部关键词区域和只读结果条目，覆盖浅色、深色及大字体。 |
| `SingleTrackPreview()` | 单首歌曲、选中背景以及默认不显示末尾分隔线。 |
| `HomePreview()` | 首页固定搜索框与列表，多首歌曲、缺失歌手和当前项高亮。 |

以上清单涵盖当前所有带 `@Composable` 的命名函数；Theme 的组合 getter 单独列在第8节。临时局部内容 lambda（例如按钮内部 `body`）作为所属组件的实现细节，不是独立调用入口。

## 10. 常见组合示例

下面的演示函数用于说明接线方式，不是项目已有 API。放在项目模块中时使用以下常用导入；涉及菜单资源的示例还需导入 `top.stellortus.stellar_music.R`，涉及播放器的示例需要后文注明的导入。

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import top.stellortus.stellar_music.ui.components.*
import top.stellortus.stellar_music.ui.theme.StellarTheme
```

### 10.1 表单提交

页面持有输入与提交状态，组件只处理外观和输入事件。示例将业务提交上提为回调：

```kotlin
@Composable
fun RenameFormDemo(submitting: Boolean, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    FormCard {
        StellarTextField(name, { name = it }, "歌单名称", Modifier.fillMaxWidth(),
            enabled = !submitting)
        StellarButton(onClick = { onSave(name.trim()) },
            kind = StellarButtonKind.Secondary,
            enabled = name.isNotBlank(), loading = submitting) {
            StellarText(if (submitting) "保存中…" else "保存")
        }
    }
}
```

### 10.2 确认删除

```kotlin
@Composable
fun DeleteDialogDemo(show: Boolean, submitting: Boolean, onClose: () -> Unit, onDelete: () -> Unit) {
    if (show) {
        StellarAlertDialog(
            title = "删除歌单",
            onDismissRequest = { if (!submitting) onClose() },
            text = { StellarText("删除后无法恢复该歌单。") },
            confirmButton = {
                StellarButton(onDelete, kind = StellarButtonKind.Danger, loading = submitting) {
                    StellarText("删除")
                }
            },
            dismissButton = {
                StellarButton(onClose, kind = StellarButtonKind.Text, enabled = !submitting) {
                    StellarText("取消")
                }
            }
        )
    }
}
```

`onDelete` 的业务逻辑需要在成功后调用 `onClose`；失败时保留弹窗，调用 Toast。组件不会隐式关闭。

### 10.3 锚定更多菜单

```kotlin
@Composable
fun MoreMenuDemo(onRename: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        StellarButton(onClick = { expanded = true }, kind = StellarButtonKind.Text) {
            StellarText("更多")
        }
        StellarActionMenu(expanded, onDismiss = { expanded = false }, actions = listOf(
            StellarMenuAction("重命名", R.drawable.ic_edit, onRename),
            StellarMenuAction("删除", R.drawable.ic_delete, onDelete, StellarMenuActionKind.Danger)
        ))
    }
}
```

### 10.4 多选歌曲

示例额外导入 `top.stellortus.stellar_music_common.dto.Track`：

```kotlin
@Composable
fun TrackChoicesDemo(tracks: List<Track>, existingIds: Set<Int>, onConfirm: (List<Int>) -> Unit) {
    var selectedIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    Column {
        tracks.forEach { track ->
            val alreadyAdded = track.id in existingIds
            TrackChoiceRow(track, selected = alreadyAdded || track.id in selectedIds,
                alreadyAdded = alreadyAdded) {
                selectedIds = if (track.id in selectedIds) selectedIds - track.id else selectedIds + track.id
            }
        }
        StellarButton(onClick = { onConfirm(selectedIds.toList()) }, enabled = selectedIds.isNotEmpty()) {
            StellarText("添加")
        }
    }
}
```

少量条目可用上面的 `Column`；实际曲库应使用 `LazyColumn`，示例不代替分页、加载和失效数据处理。若还需要自动加载曲库，优先使用 `AddTracksDialog`。

### 10.5 播放器接入与释放

示例需要以下额外导入。播放器宿主应高于会切换的子页面：

```kotlin
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import top.stellortus.stellar_music.ui.player.MusicPlayerState
import top.stellortus.stellar_music.ui.player.PlayerBar
```

```kotlin
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun PlayerHostDemo(content: @Composable (MusicPlayerState) -> Unit) {
    val context = LocalContext.current
    val state = remember { MusicPlayerState(context) }
    DisposableEffect(state) {
        onDispose { state.release() }
    }
    StellarScaffold(bottomBar = { PlayerBar(state) }) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            content(state)
        }
    }
}
```

正式宿主还应像 `MainScreen` 一样，在 `LaunchedEffect(state.playbackError)` 中提示并消费播放错误。不要在每个歌曲条目或每次重组时创建播放器；不要将秒值直接当作毫秒传给进度回调。

## 11. 维护与核对

- 修改参数、默认值或枚举时，同步更新本手册对应小节及示例。
- 新增 Composable 时，根据可见性归入基础组件、业务组件、页面或内部附录；Preview 单独登记。
- 示例中的基础控件都来自 `ui/components`；不要为了示例方便直接使用 Material 控件或硬编码颜色、圆角和 dp/sp。
- 错误提示使用 `ToastManager`，并放在事件或副作用中；页面状态与数据请求仍由业务层负责。
- 本文描述实际代码行为，尤其不要将「会请求数据的业务弹窗」与「仅提供外观的基础浮层」混用。
