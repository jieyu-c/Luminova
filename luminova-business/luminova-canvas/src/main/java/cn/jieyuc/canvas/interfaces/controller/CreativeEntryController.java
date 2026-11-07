package cn.jieyuc.canvas.interfaces.controller;

import cn.jieyuc.canvas.application.workspace.CreativeEntryView;
import cn.jieyuc.canvas.application.workspace.WorkspaceEntryApplicationService;
import cn.jieyuc.canvas.interfaces.dto.CreateCreativeEntryRequest;
import cn.jieyuc.canvas.interfaces.dto.MoveCreativeEntryRequest;
import cn.jieyuc.canvas.interfaces.dto.ReorderCreativeEntryRequest;
import cn.jieyuc.canvas.interfaces.dto.UpdateCreativeEntryRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * 工作台创作条目 HTTP Controller。
 *
 * <p>只完成协议转换和参数校验；目标用户始终由应用层从登录会话取得。</p>
 */
@Validated
@RestController
@RequestMapping("/api/v1/creative-entries")
public class CreativeEntryController {
    private final WorkspaceEntryApplicationService service;

    public CreativeEntryController(WorkspaceEntryApplicationService service) {
        this.service = service;
    }

    /** 分页查询根目录或指定文件夹的直属条目。 */
    @GetMapping
    public List<CreativeEntryView> list(@RequestParam(required = false) Long parentId,
                                        @RequestParam(defaultValue = "0") @Min(0) int page,
                                        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(parentId, page, size);
    }

    /** 按名称搜索当前用户的文件夹和画布。 */
    @GetMapping("/search")
    public List<CreativeEntryView> search(@RequestParam String q,
                                          @RequestParam(defaultValue = "0") @Min(0) int page,
                                          @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.search(q, page, size);
    }

    /** 创建文件夹。 */
    @PostMapping("/folders")
    public ResponseEntity<CreativeEntryView> createFolder(@Valid @RequestBody CreateCreativeEntryRequest request) {
        CreativeEntryView view = service.createFolder(request.parentId(), request.name());
        return ResponseEntity.created(URI.create("/api/v1/creative-entries/" + view.id())).body(view);
    }

    /** 创建空画布条目。 */
    @PostMapping("/canvases")
    public ResponseEntity<CreativeEntryView> createCanvas(@Valid @RequestBody CreateCreativeEntryRequest request) {
        CreativeEntryView view = service.createCanvas(request.parentId(), request.name());
        return ResponseEntity.created(URI.create("/api/v1/creative-entries/" + view.id())).body(view);
    }

    /** 修改名称或自定义封面。 */
    @PatchMapping("/{entryId}")
    public CreativeEntryView update(@PathVariable long entryId,
                                    @Valid @RequestBody UpdateCreativeEntryRequest request) {
        boolean coverProvided = request.customCoverId() != null || Boolean.TRUE.equals(request.clearCustomCover());
        return service.update(entryId, request.name(), request.customCoverId(), coverProvided);
    }

    /** 移动条目到目标文件夹或根目录。 */
    @PostMapping("/{entryId}/move")
    public CreativeEntryView move(@PathVariable long entryId,
                                  @RequestBody MoveCreativeEntryRequest request) {
        return service.move(entryId, request.targetParentId());
    }

    /** 调整条目手动顺序。 */
    @PostMapping("/{entryId}/reorder")
    public CreativeEntryView reorder(@PathVariable long entryId,
                                     @Valid @RequestBody ReorderCreativeEntryRequest request) {
        return service.reorder(entryId, request.sortOrder());
    }

    /** 软删除条目。 */
    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> delete(@PathVariable long entryId) {
        service.delete(entryId);
        return ResponseEntity.noContent().build();
    }

    /** 恢复软删除条目。 */
    @PostMapping("/{entryId}/restore")
    public CreativeEntryView restore(@PathVariable long entryId) {
        return service.restore(entryId);
    }

    /** 记录画布打开行为。 */
    @PostMapping("/{entryId}/opened")
    public ResponseEntity<Void> recordOpened(@PathVariable long entryId) {
        service.recordOpened(entryId);
        return ResponseEntity.noContent().build();
    }

    /** 查询最近打开画布。 */
    @GetMapping("/recently-opened")
    public List<CreativeEntryView> recentlyOpened(
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return service.recentlyOpened(limit);
    }
}
