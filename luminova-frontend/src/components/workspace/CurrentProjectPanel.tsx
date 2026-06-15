import { MoreHorizontal, Settings2 } from 'lucide-react';
import { currentProject, projectTasks, workflowSteps } from '../../data/workspace';
import { cn } from '../../lib/cn';

const taskStatusClass: Record<(typeof projectTasks)[number]['status'], string> = {
  pending: 'task-status--pending',
  'in-progress': 'task-status--active',
  'pending-review': 'task-status--review',
};

export function CurrentProjectPanel() {
  return (
    <section className="current-project" aria-label="当前项目">
      <div className="section-head">
        <div>
          <h2>
            当前项目 <span>/ {currentProject.title}</span>
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
