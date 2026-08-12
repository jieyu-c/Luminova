import { CheckCircle2, MoreHorizontal, Settings2, Sparkles } from 'lucide-react';
import { projectTasks, workflowSteps, type TableProject } from '../../data/workspace';
import { cn } from '../../lib/cn';

const taskStatusClass: Record<(typeof projectTasks)[number]['status'], string> = {
  pending: 'task-status--pending',
  'in-progress': 'task-status--active',
  'pending-review': 'task-status--review',
};

type CurrentProjectPanelProps = {
  project: TableProject | null;
};

export function CurrentProjectPanel({ project }: CurrentProjectPanelProps) {
  if (!project) {
    return (
      <section className="current-project current-project--empty" aria-label="首次创作流程">
        <div className="section-head">
          <div>
            <h2>
              当前项目 <span>/ 等待创建</span>
            </h2>
          </div>
        </div>
        <div className="current-project-empty">
          <div>
            <span className="eyebrow">Creation Path</span>
            <h3>创建后，这里会变成你的项目控制台</h3>
            <p>
              Luminova 会把创意拆成可编辑节点，你可以逐步确认剧本、角色、场景、分镜和 Prompt。
            </p>
          </div>
          <ol className="first-run-flow" aria-label="首次创作流程">
            {['输入创意', '生成 Canvas', '调整节点', '确认 Prompt', '导出视频'].map((step, index) => (
              <li key={step}>
                <span>{index + 1}</span>
                <b>{step}</b>
                {index === 0 ? <CheckCircle2 size={15} aria-hidden="true" /> : null}
              </li>
            ))}
          </ol>
          <a className="btn btn-outline current-project-empty__cta" href="#workspace-ai-dock">
            <Sparkles size={15} aria-hidden="true" />
            去创建第一个项目
          </a>
        </div>
      </section>
    );
  }

  return (
    <section className="current-project" aria-label="当前项目">
      <div className="section-head">
        <div>
          <h2>
            当前项目 <span>/ {project?.title ?? '等待创建'}</span>
          </h2>
        </div>
        <div className="section-head__actions">
          <button className="btn btn-quiet" type="button">
            <Settings2 size={15} aria-hidden="true" />
            项目设置
          </button>
          <button className="btn btn-icon" type="button" aria-label="更多操作">
            <MoreHorizontal size={18} aria-hidden="true" />
          </button>
        </div>
      </div>

      <ol className="workflow-stepper" aria-label="项目进度">
        {workflowSteps.map((step, index) => (
          <li
            key={step.id}
            className={cn('workflow-step', `workflow-step--${step.status}`)}
          >
            <span className="workflow-step__marker" aria-hidden="true">
              {step.status === 'done' ? '✓' : index + 1}
            </span>
            <span className="workflow-step__label">{step.label}</span>
            {step.statusLabel ? (
              <span className="workflow-step__status">{step.statusLabel}</span>
            ) : null}
          </li>
        ))}
      </ol>

      <div className="task-table-wrap">
        <table className="task-table">
          <caption className="sr-only">当前项目任务列表</caption>
          <tbody>
            {projectTasks.map((task) => (
              <tr key={task.id}>
                <td className="task-table__index">
                  <span>{task.index}</span>
                </td>
                <td className="task-table__main">
                  <b>{task.title}</b>
                  <p>{task.description}</p>
                </td>
                <td className="task-table__assignees">
                  <div className="avatar-stack" aria-label={`负责人 ${task.assignees.join('、')}`}>
                    {task.assignees.map((initial) => (
                      <span key={initial}>{initial}</span>
                    ))}
                  </div>
                </td>
                <td className="task-table__status">
                  <span className={cn('task-status', taskStatusClass[task.status])}>
                    {task.statusLabel}
                  </span>
                </td>
                <td className="task-table__action">
                  <button className="btn btn-quiet btn-sm" type="button">
                    去处理
                  </button>
                  <button className="btn btn-icon btn-icon--subtle" type="button" aria-label="任务菜单">
                    <MoreHorizontal size={16} aria-hidden="true" />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
