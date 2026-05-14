<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="书目ID" prop="bookId">
        <el-input v-model="queryParams.bookId" placeholder="请输入书目ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="未上架" :value="0" />
          <el-option label="已上架" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-check"
          size="mini"
          :disabled="multiple"
          @click="handleBatchShelve"
          v-hasPermi="['system:upLog:shelve']"
        >批量上架</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-close"
          size="mini"
          :disabled="multiple"
          @click="handleBatchUnshelve"
          v-hasPermi="['system:upLog:unshelve']"
        >批量下架</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:upLog:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="upLogList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="记录ID" align="center" prop="id" width="140" />
      <el-table-column label="书目ID" align="center" prop="bookId" width="180" />
      <el-table-column label="用户ID" align="center" prop="userId" width="180" />
      <el-table-column label="状态" align="center" prop="status" width="160">
        <template slot-scope="scope">
          <el-tag :type="getUpLogVO(scope.row).getStatusTagType()">
            {{ getUpLogVO(scope.row).getStatusText() }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="上架时间" align="center" prop="createTime" width="240">
        <template slot-scope="scope">
          <span>{{ getUpLogVO(scope.row).getFormattedCreateTime() }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="240">
        <template slot-scope="scope">
          <span>{{ getUpLogVO(scope.row).getFormattedUpdateTime() }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            v-if="getUpLogVO(scope.row).isUnshelved()"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleShelve(scope.row)"
            v-hasPermi="['system:upLog:shelve']"
          >上架</el-button>
          <el-button
            v-if="getUpLogVO(scope.row).isShelved()"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleUnshelve(scope.row)"
            v-hasPermi="['system:upLog:unshelve']"
          >下架</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['system:upLog:query']"
          >详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:upLog:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 查看详情对话框 -->
    <el-dialog title="上架记录详情" :visible.sync="viewOpen" width="800px" append-to-body>
      <!-- 上架记录基本信息 -->
      <el-descriptions title="上架记录信息" :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="记录ID">{{ viewData.id }}</el-descriptions-item>
        <el-descriptions-item label="书目ID">{{ viewData.bookId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ viewData.userId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewDataVO.getStatusTagType()">
            {{ viewDataVO.getStatusText() }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上架时间" :span="2">
          {{ viewDataVO.getFormattedCreateTime() }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">
          {{ viewDataVO.getFormattedUpdateTime() }}
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 书籍详细信息 -->
      <el-descriptions v-if="bookData" title="书籍详细信息" :column="2" border>
        <el-descriptions-item label="书籍ID">{{ bookDataVO.id }}</el-descriptions-item>
        <el-descriptions-item label="书籍名称">{{ bookDataVO.name }}</el-descriptions-item>
        <el-descriptions-item label="书籍类型">{{ bookDataVO.type }}</el-descriptions-item>
        <el-descriptions-item label="文笔风格">{{ bookDataVO.style }}</el-descriptions-item>
        <el-descriptions-item label="文章长度">{{ bookDataVO.wordsType }}</el-descriptions-item>
        <el-descriptions-item label="支线数量">{{ bookDataVO.branchNum }}</el-descriptions-item>
        <el-descriptions-item label="当前章数">{{ bookDataVO.chapterNum }}</el-descriptions-item>
        <el-descriptions-item label="总章数">{{ bookDataVO.totalChapter }}</el-descriptions-item>
        <el-descriptions-item label="书籍世界观" :span="2">{{ bookDataVO.world }}</el-descriptions-item>
        <el-descriptions-item label="书籍主旨" :span="2">{{ bookDataVO.core }}</el-descriptions-item>
        <el-descriptions-item label="书籍开篇" :span="2">{{ bookDataVO.beginning }}</el-descriptions-item>
        <el-descriptions-item label="书籍发展" :span="2">{{ bookDataVO.development }}</el-descriptions-item>
        <el-descriptions-item label="书籍转折点" :span="2">{{ bookDataVO.turningPoint }}</el-descriptions-item>
        <el-descriptions-item label="书籍高潮" :span="2">{{ bookDataVO.climax }}</el-descriptions-item>
        <el-descriptions-item label="书籍结尾" :span="2">{{ bookDataVO.ending }}</el-descriptions-item>
        <el-descriptions-item label="篇幅控制" :span="2">{{ bookDataVO.control }}</el-descriptions-item>
      </el-descriptions>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listUpLog, listAllUpLog, shelveUpLog, unshelveUpLog, delUpLog, batchDelUpLog, getOneBook } from "@/api/upLog"
import { UpLogVO, BookVO } from "@/vo/upLog"

export default {
  name: "Index",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 上架记录表格数据
      upLogList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      viewOpen: false,
      // 详情数据
      viewData: {},
      // 查看数据VO对象
      viewDataVO: new UpLogVO(),
      // 书籍详情数据
      bookData: null,
      // 书籍详情VO对象
      bookDataVO: new BookVO(),
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        bookId: null,
        userId: null,
        status: null
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /**
     * 判断是否有查询条件
     * @returns {boolean}
     */
    hasQueryCondition() {
      return this.queryParams.bookId !== null && this.queryParams.bookId !== undefined && this.queryParams.bookId !== '' ||
             this.queryParams.userId !== null && this.queryParams.userId !== undefined && this.queryParams.userId !== '' ||
             this.queryParams.status !== null && this.queryParams.status !== undefined && this.queryParams.status !== ''
    },

    /** 查询上架记录列表 */
    getList() {
      this.loading = true
      
      // 根据是否有查询条件选择不同的API和参数
      if (this.hasQueryCondition()) {
        // 有条件查询：使用listUpLog，传递所有查询参数
        const params = {
          pageNum: this.queryParams.pageNum,
          pageSize: this.queryParams.pageSize,
          bookId: this.queryParams.bookId,
          userId: this.queryParams.userId,
          status: this.queryParams.status
        }
        
        listUpLog(params).then(response => {
          this.upLogList = response.rows
          this.total = response.total
          this.loading = false
        }).catch(error => {
          console.warn('[Index] 获取上架记录列表失败:', error.message)
          this.upLogList = []
          this.total = 0
          this.loading = false
        })
      } else {
        // 无条件查询：使用listAllUpLog，只传递分页参数
        const params = {
          pageNum: this.queryParams.pageNum,
          pageSize: this.queryParams.pageSize
        }
        
        listAllUpLog(params).then(response => {
          this.upLogList = response.rows
          this.total = response.total
          this.loading = false
        }).catch(error => {
          console.warn('[Index] 获取上架记录列表失败:', error.message)
          this.upLogList = []
          this.total = 0
          this.loading = false
        })
      }
    },

    /** 转换上架记录数据结构 */
    getUpLogVO(row) {
      return new UpLogVO(row)
    },

    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },

    /** 上架操作 */
    handleShelve(row) {
      const id = row.id
      this.$modal.confirm('确认要上架该记录吗？').then(() => {
        return shelveUpLog(id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("上架成功")
      }).catch(() => {})
    },

    /** 下架操作 */
    handleUnshelve(row) {
      const id = row.id
      this.$modal.confirm('确认要下架该记录吗？').then(() => {
        return unshelveUpLog(id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("下架成功")
      }).catch(() => {})
    },

    /** 批量上架 */
    handleBatchShelve() {
      if (this.ids.length === 0) {
        this.$modal.msgWarning("请选择需要上架的记录")
        return
      }

      this.$modal.confirm(`确认要上架选中的 ${this.ids.length} 条记录吗？`).then(() => {
        const promises = this.ids.map(id => shelveUpLog(id))
        return Promise.all(promises)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("批量上架成功")
      }).catch(() => {})
    },

    /** 批量下架 */
    handleBatchUnshelve() {
      if (this.ids.length === 0) {
        this.$modal.msgWarning("请选择需要下架的记录")
        return
      }

      this.$modal.confirm(`确认要下架选中的 ${this.ids.length} 条记录吗？`).then(() => {
        const promises = this.ids.map(id => unshelveUpLog(id))
        return Promise.all(promises)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("批量下架成功")
      }).catch(() => {})
    },

    /** 查看详情 */
    handleView(row) {
      this.viewData = row
      this.viewDataVO = new UpLogVO(row)
      
      // 调用getOneBook接口获取书籍详细信息
      if (row.bookId) {
        getOneBook(row.bookId).then(response => {
          this.bookData = response.data
          this.bookDataVO = new BookVO(this.bookData)
          this.viewOpen = true
        }).catch(error => {
          console.warn('[Index] 获取书籍详情失败:', error.message)
          this.bookData = null
          this.bookDataVO = new BookVO()
          this.viewOpen = true
        })
      } else {
        this.bookData = null
        this.bookDataVO = new BookVO()
        this.viewOpen = true
      }
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const deleteIds = row.id || this.ids
      this.$modal.confirm('是否确认删除上架记录编号为"' + deleteIds + '"的数据项？').then(() => {
        if (Array.isArray(deleteIds) && deleteIds.length > 1) {
          return batchDelUpLog(deleteIds)
        } else {
          return delUpLog(Array.isArray(deleteIds) ? deleteIds[0] : deleteIds)
        }
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
