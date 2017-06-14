package com.kyy.demo.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.List;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;

public class MybatisPlugin extends PluginAdapter {

	private ShellCallback shellCallback = new DefaultShellCallback(false);

	public boolean sqlMapGenerated(final GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
		try {
			File file = shellCallback.getDirectory(sqlMap.getTargetProject(), sqlMap.getTargetPackage());
			if (file.isDirectory()) {
				File[] files = file.listFiles(new FilenameFilter() {

					public boolean accept(File dir, String name) {
						return name.equals(sqlMap.getFileName());
					}
				});
				if (files.length > 0) {
					System.out.println("Delete file " + files[0].getAbsolutePath());
					files[0].delete();
				}
			}
		} catch (ShellException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		if (!introspectedTable.getPrimaryKeyColumns().isEmpty()) {
			Method method = new Method();
			method.setVisibility(JavaVisibility.PUBLIC);
			method.setName("toString");
			method.setReturnType(FullyQualifiedJavaType.getStringInstance());

			StringBuilder builder = new StringBuilder("return this.getClass().getName() + ");
			for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
				builder.append("'_' + " + column.getJavaProperty());
			}
			builder.append(';');

			method.addBodyLine(builder.toString());
			topLevelClass.addMethod(method);
		}
		if ("true".equals(introspectedTable.getTableConfiguration().getProperty("autoIncrement"))) {
			topLevelClass.addSuperInterface(new FullyQualifiedJavaType("com.kyy.demo.model.Generatable"));
		}
		return true;
	}

	// private Map<IntrospectedTable, XmlElement> map = new HashMap<>();

	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		StringBuilder builder = new StringBuilder();
		Iterator<IntrospectedColumn> iterator = introspectedTable.getAllColumns().iterator();
		while (iterator.hasNext()) {
			builder.append(iterator.next().getActualColumnName());
			if (iterator.hasNext()) {
				builder.append(',').append(' ');
			}
		}
		String columns = builder.toString();

		// 分页前缀sql
		XmlElement pagePrefixSql = new XmlElement("sql");
		pagePrefixSql.addAttribute(new Attribute("id", "pagePrefix"));
		pagePrefixSql
				.addElement(new TextElement("select " + columns + " from (select " + columns + ", ROWNUM RN from ("));
		document.getRootElement().addElement(pagePrefixSql);

		// 分页后缀sql
		XmlElement pageSuffixSql = new XmlElement("sql");
		pageSuffixSql.addAttribute(new Attribute("id", "pageSuffix"));
		pageSuffixSql.addElement(new TextElement(
				"<![CDATA[) where ROWNUM <= #{page.end,jdbcType=DECIMAL}) where RN >= #{page.begin,jdbcType=DECIMAL}]]>"));
		document.getRootElement().addElement(pageSuffixSql);

		// select list sql
		XmlElement listSql = new XmlElement("select");
		listSql.addAttribute(new Attribute("id", "selectList"));
		listSql.addAttribute(new Attribute("resultMap", "BaseResultMap"));
		listSql.addAttribute(new Attribute("parameterType", "java.util.Map"));

		XmlElement prefixIfElement = new XmlElement("if");
		prefixIfElement.addAttribute(new Attribute("test", "page != null"));
		XmlElement prefixIncludeElement = new XmlElement("include");
		prefixIncludeElement.addAttribute(new Attribute("refid", "pagePrefix"));
		prefixIfElement.addElement(prefixIncludeElement);
		listSql.addElement(prefixIfElement);
		listSql.addElement(new TextElement("select " + columns + " from "
				+ introspectedTable.getTableConfiguration().getTableName().toUpperCase()));
		XmlElement suffixIfElement = new XmlElement("if");
		suffixIfElement.addAttribute(new Attribute("test", "page != null"));
		XmlElement suffixIncludeElement = new XmlElement("include");
		suffixIncludeElement.addAttribute(new Attribute("refid", "pageSuffix"));
		suffixIfElement.addElement(suffixIncludeElement);
		listSql.addElement(suffixIfElement);

		// 排序 sql
		XmlElement orderElement = new XmlElement("if");
		orderElement.addAttribute(new Attribute("test", "order != null"));
		orderElement.addElement(new TextElement("order by ${order}"));
		listSql.addElement(orderElement);

		document.getRootElement().addElement(listSql);

		XmlElement selectCount = new XmlElement("select");
		selectCount.addAttribute(new Attribute("id", "selectCount"));
		selectCount.addAttribute(new Attribute("resultType", "java.lang.Long"));
		selectCount.addElement(
				new TextElement("select count(1) from " + introspectedTable.getTableConfiguration().getTableName()));

		document.getRootElement().addElement(selectCount);

		// select sequence.nextval
		if ("true".equals(introspectedTable.getTableConfiguration().getProperty("autoIncrement"))) {
			if (introspectedTable.getPrimaryKeyColumns().size() != 1) {
				throw new IllegalStateException("can't use auto increment for multi primary key columns");
			}
			String sequenceName = introspectedTable.getTableConfiguration().getProperty("sequence");
			if (sequenceName == null) {
				sequenceName = "sequence_"
						+ introspectedTable.getTableConfiguration().getDomainObjectName().toLowerCase() + '_'
						+ introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName().toLowerCase();
			}
			XmlElement selectNextIdSql = new XmlElement("select");
			selectNextIdSql.addAttribute(new Attribute("id", "selectSequenceNextVal"));
			selectNextIdSql.addAttribute(new Attribute("resultType", "java.lang.Long"));
			selectNextIdSql
					.addElement(new TextElement("select " + getSequenceName(introspectedTable) + ".nextval from dual"));

			document.getRootElement().addElement(selectNextIdSql);
			// XmlElement xmlElement = map.get(introspectedTable);
			// if (xmlElement != null) {
			// List<Attribute> list = xmlElement.getAttributes();
			// for (int i = 0; i < list.size(); i++) {
			// if ("id".equals(list.get(i).getName())) {
			// xmlElement.getAttributes().remove(i);
			// xmlElement.getAttributes().add(i, new Attribute("id",
			// "insert2"));
			// break;
			// }
			// }
			// document.getRootElement().addElement(xmlElement);
			// }
		}
		// if
		// ("true".equals(introspectedTable.getTableConfiguration().getProperty("batchDelete")))
		// {
		// if (introspectedTable.getPrimaryKeyColumns().size() != 1) {
		// throw new IllegalStateException("can't use batch delete for multi
		// primary key columns");
		// }
		// XmlElement batchDelete = new XmlElement("delete");
		// batchDelete.addAttribute(new Attribute("id", "batchDelete"));
		// batchDelete.addElement(
		// new TextElement("delete from " +
		// introspectedTable.getTableConfiguration().getTableName()
		// + " where " +
		// introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName()
		// + " in "));
		// XmlElement forEach = new XmlElement("foreach");
		// forEach.addAttribute(new Attribute("collection", "array"));
		// forEach.addAttribute(new Attribute("item", "item"));
		// forEach.addAttribute(new Attribute("index", "index"));
		// forEach.addAttribute(new Attribute("open", "("));
		// forEach.addAttribute(new Attribute("close", ")"));
		// forEach.addAttribute(new Attribute("separator", ","));
		// forEach.addElement(new TextElement("#{item,jdbcType=" +
		// introspectedTable.getPrimaryKeyColumns().get(0).getJdbcTypeName() +
		// "}"));
		// batchDelete.addElement(forEach);
		// document.getRootElement().addElement(batchDelete);
		// }

		return true;
	}

	// @Override
	// public boolean sqlMapInsertElementGenerated(XmlElement element,
	// IntrospectedTable introspectedTable) {
	// if
	// ("true".equals(introspectedTable.getTableConfiguration().getProperty("autoIncrement")))
	// {
	// map.put(introspectedTable, copy(element));
	// if (introspectedTable.getPrimaryKeyColumns().size() == 1) {
	// String primaryKeyColumnName =
	// introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName()
	// .toLowerCase();
	// String temp = "#{" + primaryKeyColumnName + ",jdbcType="
	// + introspectedTable.getPrimaryKeyColumns().get(0).getJdbcTypeName() +
	// "}";
	// String newSql = null;
	// for (int i = 0; i < element.getElements().size(); i++) {
	// Element item = null;
	// if ((item = element.getElements().get(i)) instanceof TextElement) {
	// String sql = ((TextElement) item).getContent();
	// int index = 0;
	// if ((index = sql.indexOf(temp)) >= 0) {
	// newSql = sql.substring(0, index) + getSequenceName(introspectedTable) +
	// ".nextval"
	// + sql.substring(index + temp.length());
	// element.getElements().remove(i);
	// element.getElements().add(i, new TextElement(newSql));
	// }
	// }
	// }
	// } else {
	// throw new IllegalStateException("can't use auto increment for multi
	// primary key columns");
	// }
	// }
	// return super.sqlMapInsertElementGenerated(element, introspectedTable);
	// }

	// private XmlElement copy(XmlElement element) {
	// XmlElement e = new XmlElement(element.getName());
	// for (Attribute attr : element.getAttributes()) {
	// e.addAttribute(new Attribute(attr.getName(), attr.getValue()));
	// }
	// for (Element item : element.getElements()) {
	// if (item instanceof XmlElement) {
	// e.addElement(copy((XmlElement) item));
	// } else if (item instanceof TextElement) {
	// e.addElement(new TextElement(((TextElement) item).getContent()));
	// }
	// }
	// return e;
	// }

	private String getSequenceName(IntrospectedTable introspectedTable) {
		String sequenceName = introspectedTable.getTableConfiguration().getProperty("sequence");
		if (sequenceName == null) {
			sequenceName = "sequence_" + introspectedTable.getTableConfiguration().getDomainObjectName().toLowerCase()
					+ '_' + introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName().toLowerCase();
		}
		return sequenceName;
	}

	public boolean validate(List<String> arg0) {
		return true;
	}

}
