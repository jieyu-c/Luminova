import { useMemo, useState } from "react";
import {
  ArrowLeft, CaretDown, CaretRight, ClockCounterClockwise, DotsThree, FilmSlate,
  Folder, FolderOpen, GridFour, House, ListBullets, MagnifyingGlass, PencilSimple,
  Plus, SlidersHorizontal, Trash, X,
} from "@phosphor-icons/react";

type Entry = {
  id: number;
  type: "folder" | "canvas";
  name: string;
  modified: string;
  meta: string;
  image?: string;
  position?: string;
};

const entries: Entry[] = [
  { id: 1, type: "folder", name: "长篇电影", modified: "今天 17:03", meta: "3 个子项" },
  { id: 2, type: "canvas", name: "雾中来信", modified: "今天 15:20", meta: "画布", image: "/assets/luminova-carousel-first-light.png", position: "42% center" },
  { id: 3, type: "canvas", name: "山海之间", modified: "昨天 21:47", meta: "画布", image: "/assets/luminova-film-montage-five-wide.png", position: "48% center" },
  { id: 4, type: "folder", name: "灵感碎片", modified: "昨天 18:09", meta: "12 个子项" },
  { id: 5, type: "canvas", name: "旧城夜航", modified: "1月24日 20:18", meta: "画布", image: "/assets/luminova-seamless-film-panorama.png", position: "76% center" },
  { id: 6, type: "canvas", name: "无声片场", modified: "1月23日 11:06", meta: "画布", image: "/assets/luminova-homepage-cinematic-triptych.png", position: "95% center" },
];

const recent = entries.filter((entry) => entry.type === "canvas").slice(0, 3);

type WorkspaceProps = { onExit: () => void };

export function Workspace({ onExit }: WorkspaceProps) {
  const [activeNav, setActiveNav] = useState<"workspace" | "recent" | "trash">("workspace");
  const [view, setView] = useState<"grid" | "list">("grid");
  const [query, setQuery] = useState("");
  const [sortOpen, setSortOpen] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [menuId, setMenuId] = useState<number | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [dialog, setDialog] = useState<"rename" | "move" | "new-folder" | "new-canvas" | null>(null);
  const [draftName, setDraftName] = useState("");
  const [toast, setToast] = useState("");

  const visibleEntries = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    return normalized ? entries.filter((entry) => entry.name.toLowerCase().includes(normalized)) : entries;
  }, [query]);

  const openDialog = (next: typeof dialog, entry?: Entry) => {
    setDialog(next);
    setDraftName(entry?.name ?? "");
    setMenuId(null);
    setCreateOpen(false);
  };

  const completeAction = (message: string) => {
    setDialog(null);
    setToast(message);
    window.setTimeout(() => setToast(""), 2400);
  };

  return (
    <div className="workspace-shell">
      <aside className="workspace-sidebar">
        <button className="workspace-brand" type="button" onClick={onExit}>Luminova</button>
        <nav className="workspace-nav" aria-label="工作台导航">
          <button className={activeNav === "workspace" ? "is-active" : ""} onClick={() => setActiveNav("workspace")}><House weight="light" /> 工作台</button>
          <button className={activeNav === "recent" ? "is-active" : ""} onClick={() => setActiveNav("recent")}><ClockCounterClockwise weight="light" /> 最近打开</button>
          <button className={activeNav === "trash" ? "is-active" : ""} onClick={() => setActiveNav("trash")}><Trash weight="light" /> 回收站</button>
        </nav>

        <div className="workspace-tree">
          <p>目录</p>
          <button className="tree-row is-open"><CaretDown /><FolderOpen /> 根目录</button>
          <button className="tree-row is-open tree-level-1"><CaretDown /><FolderOpen /> 长篇电影</button>
          <button className="tree-row is-open tree-level-2"><CaretDown /><FolderOpen /> 剧本素材</button>
          <button className="tree-row tree-level-3"><span /><Folder /> 人物</button>
          <button className="tree-row tree-level-3"><span /><Folder /> 场景</button>
          <button className="tree-row tree-level-1"><CaretRight /><Folder /> 视觉参考</button>
          <button className="tree-row tree-level-1"><CaretRight /><Folder /> 短片创作</button>
          <button className="tree-row tree-level-1"><CaretRight /><Folder /> 个人素材库</button>
        </div>

        <div className="workspace-user">
          <img src="/assets/luminova-carousel-first-light.png" alt="林晚" />
          <div><strong>林晚</strong><span>个人创作空间</span></div>
          <CaretDown />
        </div>
      </aside>

      <main className="workspace-main">
        <header className="workspace-topbar">
          <div className="workspace-breadcrumb"><span>根目录</span><i>/</i><strong>长篇电影</strong></div>
          <label className="workspace-search"><MagnifyingGlass /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="搜索画布与文件夹" />{query && <button onClick={() => setQuery("")} aria-label="清除搜索"><X /></button>}</label>
          <div className="workspace-top-actions">
            <div className="view-switch" aria-label="视图切换"><button className={view === "grid" ? "is-active" : ""} onClick={() => setView("grid")}><GridFour /></button><button className={view === "list" ? "is-active" : ""} onClick={() => setView("list")}><ListBullets /></button></div>
            <div className="workspace-popover-wrap">
              <button className="quiet-control" onClick={() => setSortOpen((open) => !open)}><SlidersHorizontal /> 最近修改 <CaretDown /></button>
              {sortOpen && <div className="workspace-popover sort-popover"><button>手动排序</button><button>名称</button><button className="is-current">最近修改</button><button>创建时间</button></div>}
            </div>
            <div className="workspace-popover-wrap">
              <button className="create-button" onClick={() => setCreateOpen((open) => !open)}><Plus /> 新建 <CaretDown /></button>
              {createOpen && <div className="workspace-popover create-popover"><button onClick={() => openDialog("new-canvas")}><FilmSlate /> 新建画布</button><button onClick={() => openDialog("new-folder")}><Folder /> 新建文件夹</button></div>}
            </div>
          </div>
        </header>

        <div className="workspace-scroll">
          {activeNav === "trash" ? (
            <TrashView onRestore={() => completeAction("已恢复到根目录")} />
          ) : (
            <>
              <section className="recent-section">
                <div className="workspace-section-title"><div><p>CONTINUE CREATING</p><h1>{activeNav === "recent" ? "最近打开" : "继续创作"}</h1></div><button onClick={() => setActiveNav("recent")}>查看全部 <ArrowLeft /></button></div>
                <div className="recent-grid">
                  {recent.map((entry) => <button className="recent-card" key={entry.id} onClick={() => setToast(`正在打开「${entry.name}」`)} style={{ backgroundImage: `linear-gradient(180deg, transparent 30%, rgba(5,9,9,.88)), url(${entry.image})`, backgroundPosition: entry.position }}><span><FilmSlate /> 画布</span><strong>{entry.name}</strong><small>{entry.modified}</small></button>)}
                </div>
              </section>

              <section className="directory-section">
                <div className="workspace-section-title"><div><p>CREATIVE ENTRIES</p><h2>{query ? `搜索结果 · ${visibleEntries.length}` : "当前目录"}</h2></div><span className="entry-summary">{visibleEntries.length} 个条目 · 文件夹与画布混排</span></div>
                {view === "grid" ? (
                  <div className="entry-grid">
                    {visibleEntries.map((entry) => <EntryCard key={entry.id} entry={entry} selected={selectedId === entry.id} menuOpen={menuId === entry.id} onSelect={() => setSelectedId(entry.id)} onMenu={() => setMenuId(menuId === entry.id ? null : entry.id)} onRename={() => openDialog("rename", entry)} onMove={() => openDialog("move", entry)} onTrash={() => completeAction(`「${entry.name}」已移到回收站`)} />)}
                  </div>
                ) : (
                  <div className="entry-list">
                    <div className="entry-list-head"><span>名称</span><span>类型</span><span>修改时间</span><span /></div>
                    {visibleEntries.map((entry) => <div className="entry-list-row" key={entry.id}><div>{entry.type === "folder" ? <Folder weight="fill" /> : <FilmSlate />}<strong>{entry.name}</strong></div><span>{entry.type === "folder" ? "文件夹" : "画布"}</span><span>{entry.modified}</span><button onClick={() => setMenuId(entry.id)}><DotsThree /></button></div>)}
                  </div>
                )}
              </section>
            </>
          )}
        </div>

        {dialog && <WorkspaceDialog type={dialog} name={draftName} onNameChange={setDraftName} onClose={() => setDialog(null)} onConfirm={() => completeAction(dialog === "move" ? "条目已移动到「剧本素材」" : dialog === "rename" ? `已重命名为「${draftName || "未命名"}」` : `已创建「${draftName || (dialog === "new-folder" ? "新建文件夹" : "未命名画布")}」`)} />}
        {toast && <div className="workspace-toast">{toast}</div>}
      </main>
    </div>
  );
}

function EntryCard({ entry, selected, menuOpen, onSelect, onMenu, onRename, onMove, onTrash }: { entry: Entry; selected: boolean; menuOpen: boolean; onSelect: () => void; onMenu: () => void; onRename: () => void; onMove: () => void; onTrash: () => void }) {
  return <article className={`entry-card ${entry.type} ${selected ? "is-selected" : ""}`} onClick={onSelect}>
    <div className="entry-visual">
      {entry.type === "folder" ? <div className="folder-preview"><span style={{ backgroundImage: "url(/assets/luminova-film-montage-five-wide.png)", backgroundPosition: "15%" }} /><span style={{ backgroundImage: "url(/assets/luminova-homepage-cinematic-triptych.png)", backgroundPosition: "52%" }} /><span style={{ backgroundImage: "url(/assets/luminova-seamless-film-panorama.png)", backgroundPosition: "82%" }} /></div> : <img src={entry.image} alt="" style={{ objectPosition: entry.position }} />}
      <span className="entry-type">{entry.type === "folder" ? <><Folder weight="fill" /> 文件夹</> : <><FilmSlate /> 画布</>}</span>
    </div>
    <div className="entry-info"><div><h3>{entry.name}</h3><p>{entry.meta} · {entry.modified}</p></div><button className="entry-more" onClick={(event) => { event.stopPropagation(); onMenu(); }} aria-label={`${entry.name}更多操作`}><DotsThree weight="bold" /></button></div>
    {menuOpen && <div className="entry-menu" onClick={(event) => event.stopPropagation()}><button><FilmSlate /> 打开</button><button onClick={onRename}><PencilSimple /> 重命名</button><button onClick={onMove}><FolderOpen /> 移动到…</button><button className="danger" onClick={onTrash}><Trash /> 移到回收站</button></div>}
  </article>;
}

function TrashView({ onRestore }: { onRestore: () => void }) {
  return <section className="trash-view"><div className="workspace-section-title"><div><p>RECYCLE BIN</p><h1>回收站</h1></div><span className="entry-summary">条目将在 30 天后自动清除</span></div><div className="trash-table"><div className="trash-head"><span>名称</span><span>类型</span><span>删除位置</span><span>剩余天数</span><span>操作</span></div>{["雾景素材", "旧版雾效", "雾中航行"].map((name, index) => <div className="trash-row" key={name}><strong>{index === 1 ? <FilmSlate /> : <Folder />} {name}</strong><span>{index === 1 ? "画布" : "文件夹"}</span><span>根目录 / 长篇电影</span><span>{29 - index * 2} 天</span><div><button onClick={onRestore}>恢复</button><button className="danger-text">永久删除</button></div></div>)}</div></section>;
}

function WorkspaceDialog({ type, name, onNameChange, onClose, onConfirm }: { type: "rename" | "move" | "new-folder" | "new-canvas"; name: string; onNameChange: (value: string) => void; onClose: () => void; onConfirm: () => void }) {
  const title = type === "move" ? "移动到…" : type === "rename" ? "重命名条目" : type === "new-folder" ? "新建文件夹" : "新建画布";
  return <div className="workspace-dialog-backdrop" onMouseDown={onClose}><section className="workspace-dialog" role="dialog" aria-modal="true" onMouseDown={(event) => event.stopPropagation()}><button className="workspace-dialog-close" onClick={onClose}><X /></button><p>CREATIVE ENTRY</p><h2>{title}</h2>{type === "move" ? <div className="move-tree"><label><MagnifyingGlass /><input placeholder="搜索目录" /></label><button className="is-open"><CaretDown /><FolderOpen /> 根目录</button><button className="is-open level-1"><CaretDown /><FolderOpen /> 长篇电影</button><button className="is-selected level-2"><Folder /> 剧本素材</button><button className="level-2"><Folder /> 分镜画布</button><small>画布不能作为目标目录</small></div> : <label className="dialog-name-field"><span>名称</span><input autoFocus maxLength={100} value={name} onChange={(event) => onNameChange(event.target.value)} placeholder={type === "new-folder" ? "例如：视觉参考" : "例如：雾中来信"} /><small>{name.length} / 100</small></label>}<div className="workspace-dialog-actions"><button onClick={onClose}>取消</button><button className="confirm" onClick={onConfirm}>{type === "move" ? "移动到此处" : "确认"}</button></div></section></div>;
}
