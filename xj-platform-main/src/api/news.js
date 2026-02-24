import request from "@/utils/request";

// 新增新闻
export function addNews(data) {
  return request.post("/news", data);
}

// 删除新闻
export function deleteNews(id) {
  return request.delete(`/news/${id}`);
}

// 获取所有新闻
export function getAllNews() {
  return request.get("/news/list");
}

// 获取主页新闻
export function getHomePageNews() {
  return request.get("/news/home");
}
