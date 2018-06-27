
package com.studentmanage.web;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.studentmanage.dao.StudentDao;
import com.studentmanage.model.PageBean;
import com.studentmanage.model.Student;
import com.studentmanage.util.DbUtil;
import com.studentmanage.util.PropertiesUtil;
import com.studentmanage.util.StringUtil;

public class AdminServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	DbUtil dbUtil = new DbUtil();
	StudentDao studentDao = new StudentDao();
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		HttpSession session = request.getSession();
		Object currentUserType = session.getAttribute("currentUserType");
		String s_studentText = request.getParameter("s_studentText");
		String dormBuildId = request.getParameter("buildToSelect");
		String searchType = request.getParameter("searchType");
		String page = request.getParameter("page");
		String action = request.getParameter("action");
		Student student = new Student();
		if("preSave".equals(action)) {
			adminPreSave(request, response);
			return;
		} else if("show".equals(action)) {
			adminShow(request, response);
			return;
		}else if("save".equals(action)){
			adminSave(request, response);
			return;
		} else if("delete".equals(action)){
			adminDelete(request, response);
			return;
		} else if("list".equals(action)) {
			if(StringUtil.isNotEmpty(s_studentText)) {
				if("name".equals(searchType)) {
					student.setName(s_studentText);
				} else if("number".equals(searchType)) {
					student.setStuId(s_studentText);
				} else if("dorm".equals(searchType)) {
					student.setDorNum(s_studentText);
				}
			}
//			if(StringUtil.isNotEmpty(dormBuildId)) {
//				student.setDorBuiNum(Integer.parseInt(dormBuildId));
//			}
			session.removeAttribute("s_studentText");
			session.removeAttribute("searchType");
			session.removeAttribute("buildToSelect");
			request.setAttribute("s_studentText", s_studentText);
			request.setAttribute("searchType", searchType);
			request.setAttribute("buildToSelect", dormBuildId);
		} else if("search".equals(action)){
			if(StringUtil.isNotEmpty(s_studentText)) {
				if("name".equals(searchType)) {
					student.setName(s_studentText);
				} else if("number".equals(searchType)) {
					student.setStuId(s_studentText);
				} else if("dorm".equals(searchType)) {
					student.setDorNum(s_studentText);
				}
				session.setAttribute("s_studentText", s_studentText);
				session.setAttribute("searchType", searchType);
			} else {
				session.removeAttribute("s_studentText");
				session.removeAttribute("searchType");
			}
			if(StringUtil.isNotEmpty(dormBuildId)) {
//				student.setDorBuiNum(Integer.parseInt(dormBuildId));
				session.setAttribute("buildToSelect", dormBuildId);
			}else {
				session.removeAttribute("buildToSelect");
			}
		} else {
			if("admin".equals((String)currentUserType)) {
				if(StringUtil.isNotEmpty(s_studentText)) {
					if("name".equals(searchType)) {
						student.setName(s_studentText);
					} else if("number".equals(searchType)) {
						student.setStuId(s_studentText);
					} else if("dorm".equals(searchType)) {
						student.setDorNum(s_studentText);
					}
					session.setAttribute("s_studentText", s_studentText);
					session.setAttribute("searchType", searchType);
				}
				if(StringUtil.isNotEmpty(dormBuildId)) {
//					student.setDorBuiNum(Integer.parseInt(dormBuildId));
					session.setAttribute("buildToSelect", dormBuildId);
				}
				if(StringUtil.isEmpty(s_studentText) && StringUtil.isEmpty(dormBuildId)) {
					Object o1 = session.getAttribute("s_studentText");
					Object o2 = session.getAttribute("searchType");
					if(o1!=null) {
						if("name".equals((String)o2)) {
							student.setName((String)o1);
						} else if("number".equals((String)o2)) {
							student.setStuId((String)o1);
						} else if("dorm".equals((String)o2)) {
							student.setDorNum((String)o1);
						}
					}
//					if(o3 != null) {
//						student.setDorBuiNum(Integer.parseInt((String)o3));
//					}
				}
			} else if("dormManager".equals((String)currentUserType)) {
				if(StringUtil.isNotEmpty(s_studentText)) {
					if("name".equals(searchType)) {
						student.setName(s_studentText);
					} else if("number".equals(searchType)) {
						student.setStuId(s_studentText);
					} else if("dorm".equals(searchType)) {
						student.setDorNum(s_studentText);
					}
					session.setAttribute("s_studentText", s_studentText);
					session.setAttribute("searchType", searchType);
				}
				if(StringUtil.isEmpty(s_studentText)) {
					Object o1 = session.getAttribute("s_studentText");
					Object o2 = session.getAttribute("searchType");
					if(o1!=null) {
						if("name".equals((String)o2)) {
							student.setName((String)o1);
						} else if("number".equals((String)o2)) {
							student.setStuId((String)o1);
						} else if("dorm".equals((String)o2)) {
							student.setDorNum((String)o1);
						}
					}
				}
			}
		}
		if(StringUtil.isEmpty(page)) {
			page="1";
		}
		Connection con = null;
		PageBean pageBean = new PageBean(Integer.parseInt(page), Integer.parseInt(PropertiesUtil.getValue("pageSize")));
		request.setAttribute("pageSize", pageBean.getPageSize());
		request.setAttribute("page", pageBean.getPage());
		try {
			con=dbUtil.getCon();
			List<Student> studentList =studentDao.studentList(con, pageBean, student);
			int total=studentDao.studentCount(con, student);
			String pageCode = this.genPagation(total, Integer.parseInt(page), Integer.parseInt(PropertiesUtil.getValue("pageSize")));
			request.setAttribute("pageCode", pageCode);
			request.setAttribute("studentList", studentList);
			request.setAttribute("mainPage", "admin/admin.jsp");
			request.getRequestDispatcher("mainAdmin.jsp").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dbUtil.closeCon(con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void adminDelete(HttpServletRequest request,
			HttpServletResponse response) {
		String studentId = request.getParameter("id");
		Connection con = null;
		try {
			con = dbUtil.getCon();
			studentDao.studentDelete(con, studentId);
			request.getRequestDispatcher("admin?action=list").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dbUtil.closeCon(con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	private void adminSave(HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException {
		String id=request.getParameter("id");
		String userName=request.getParameter("stuId");
		String password = request.getParameter("password");
		String grade = request.getParameter("grade");
		String name = request.getParameter("name");
		String stuId = request.getParameter("stuId");
		String teacher = request.getParameter("teacher");
		String sex = request.getParameter("sex");
		String hobby = request.getParameter("hobby");
		String specialty = request.getParameter("specialty");
		String natPlace = request.getParameter("natPlace");
		String ofPlace = request.getParameter("ofPlace");
		String nation = request.getParameter("nation");
		String status = request.getParameter("status");
		String qq = request.getParameter("qq");
		String telNumber = request.getParameter("telNumber");
		String phoModel = request.getParameter("phoModel");
		String phoImei = request.getParameter("phoImei");
		String mail = request.getParameter("mail");
		String weiChat = request.getParameter("weiChat");
		String idCard = request.getParameter("idCard");
		String fatTelNum = request.getParameter("fatTelNum");
		String motTelNum = request.getParameter("motTelNum");
		String address = request.getParameter("address");
		String famCon = request.getParameter("famCon");
		String dorBuiNum = request.getParameter("dorBuiNum");
		String dorNum = request.getParameter("dorNum");
		String bedNum = request.getParameter("bedNum");
		String don = request.getParameter("don");
		String cleTim = request.getParameter("cleTim");
		String comSecLe = request.getParameter("comSecLe");
		String cet4 = request.getParameter("cet4");
		String cet6 = request.getParameter("cet6");
		String classDut = request.getParameter("classDut");
		String stuUniDut = request.getParameter("stuUniDut");
		String acaUniDut = request.getParameter("acaUniDut");
		String schUniDut = request.getParameter("schUniDut");
		String association = request.getParameter("association");
		String pooStuFil = request.getParameter("pooStuFil");
		String pubMed = request.getParameter("pubMed");
		String illtems = request.getParameter("illtems");
		String laterTim = request.getParameter("laterTim");
		String latEarTim = request.getParameter("latEarTim");
		String latMorTim = request.getParameter("latMorTim");
		String noMorTim = request.getParameter("noMorTim");
		String criSit = request.getParameter("criSit");
		String punSit = request.getParameter("punSit");
		String comModel = request.getParameter("comModel");
		String comWifMac = request.getParameter("comWifMac");
		String comWirMac = request.getParameter("comWirMac");
		String disHis = request.getParameter("disHis");

		Student student = new Student(password,grade,name,stuId,teacher,sex,hobby,specialty,natPlace,ofPlace,nation,status,qq,telNumber,phoModel,phoImei,mail,weiChat,idCard,fatTelNum,motTelNum,address,famCon,dorBuiNum,dorNum,bedNum,don,cleTim,comSecLe,cet4,cet6,classDut,stuUniDut,acaUniDut,schUniDut,association,pooStuFil,pubMed,illtems,laterTim,latEarTim,latMorTim,noMorTim,criSit,punSit,comModel,comWifMac,comWirMac,disHis);
		if(StringUtil.isNotEmpty(id)) {
			student.setId(Integer.parseInt(id));
		}
		Connection con = null;
		try {
			con = dbUtil.getCon();
			int saveNum = 0;
			if(StringUtil.isNotEmpty(id)) {
				saveNum = studentDao.studentUpdate(con, student);
			} else if(studentDao.haveNameByNumber(con, student.getStuId())){
				request.setAttribute("admin", student);
				request.setAttribute("error", "��ѧ���Ѵ���");
				request.setAttribute("mainPage", "admin/adminSave.jsp");
				request.getRequestDispatcher("mainAdmin.jsp").forward(request, response);
				try {
					dbUtil.closeCon(con);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			} else {
				saveNum = studentDao.studentAdd(con, student);
			}
			if(saveNum > 0) {
				request.getRequestDispatcher("admin?action=list").forward(request, response);
			} else {
				request.setAttribute("admin", student);
				request.setAttribute("error", "����ʧ��");
				request.setAttribute("mainPage", "admin/adminSave.jsp");
				request.getRequestDispatcher("mainAdmin.jsp").forward(request, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dbUtil.closeCon(con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void adminPreSave(HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException {
		String studentId = request.getParameter("id");
		Connection con = null;
		try {
			con = dbUtil.getCon();
			if (StringUtil.isNotEmpty(studentId)) {
				Student student = studentDao.studentShow(con, studentId);
				request.setAttribute("admin", student);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dbUtil.closeCon(con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		request.setAttribute("mainPage", "admin/adminSave.jsp");
		request.getRequestDispatcher("mainAdmin.jsp").forward(request, response);
	}
	private void adminShow(HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException {
		String studentId = request.getParameter("id");
		Connection con = null;
		try {
			con = dbUtil.getCon();
			if (StringUtil.isNotEmpty(studentId)) {
				Student student = studentDao.studentShow(con, studentId);
				request.setAttribute("admin", student);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dbUtil.closeCon(con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		request.setAttribute("mainPage", "admin/adminShow.jsp");
		request.getRequestDispatcher("mainAdmin.jsp").forward(request, response);
	}

	private String genPagation(int totalNum, int currentPage, int pageSize){
		int totalPage = totalNum%pageSize==0?totalNum/pageSize:totalNum/pageSize+1;
		StringBuffer pageCode = new StringBuffer();
		pageCode.append("<li><a href='admin?page=1'>��ҳ</a></li>");
		if(currentPage==1) {
			pageCode.append("<li class='disabled'><a href='#'>��һҳ</a></li>");
		}else {
			pageCode.append("<li><a href='admin?page="+(currentPage-1)+"'>��һҳ</a></li>");
		}
		for(int i=currentPage-2;i<=currentPage+2;i++) {
			if(i<1||i>totalPage) {
				continue;
			}
			if(i==currentPage) {
				pageCode.append("<li class='active'><a href='#'>"+i+"</a></li>");
			} else {
				pageCode.append("<li><a href='admin?page="+i+"'>"+i+"</a></li>");
			}
		}
		if(currentPage==totalPage) {
			pageCode.append("<li class='disabled'><a href='#'>��һҳ</a></li>");
		} else {
			pageCode.append("<li><a href='admin?page="+(currentPage+1)+"'>��һҳ</a></li>");
		}
		pageCode.append("<li><a href='admin?page="+totalPage+"'>βҳ</a></li>");
		return pageCode.toString();
	}
	
}

