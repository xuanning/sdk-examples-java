package com.hitachivantara.example.hcp.management;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.hitachivantara.common.ex.HSCException;
import com.hitachivantara.example.hcp.util.HCPClients;
import com.hitachivantara.hcp.common.ex.InvalidResponseException;
import com.hitachivantara.hcp.management.api.HCPTenantManagement;
import com.hitachivantara.hcp.management.define.AclsUsage;
import com.hitachivantara.hcp.management.define.HashScheme;
import com.hitachivantara.hcp.management.define.OptimizedFor;
import com.hitachivantara.hcp.management.define.OwnerType;
import com.hitachivantara.hcp.management.define.QuotaUnit;
import com.hitachivantara.hcp.management.model.NamespaceSettings;
import com.hitachivantara.hcp.management.model.builder.SettingBuilders;

/**
 * 创建桶示例
 * 
 * @author sohan
 *
 */
public class Example_NamespaceCreate {

	public Example_NamespaceCreate() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws HSCException {
		// 需要HCP开启管理功能API,并使用管理用户
		HCPTenantManagement namespaceClient = HCPClients.getInstance().getHCPTenantManagementClient();

		String ns = "notexist-bucket-1";
		// PREPARE TEST DATA ----------------------------------------------------------------------

		// 判断桶是否存在		
		boolean exist = namespaceClient.doesNamespaceExist(ns);
		System.out.println("Namespece [" + ns + "] " + (exist ? "exist!" : "not exist!"));

		if (exist) {
			// 桶空间如果非空无法删除
			namespaceClient.deleteNamespace(ns);
		}

		exist = namespaceClient.doesNamespaceExist(ns);
		if (!exist) {
			System.out.println("Namespece [" + ns + "] deleted!");
		} else {
			System.out.println("Namespece [" + ns + "] failed to delete!");
			return;
		}

		// PREPARE TEST DATA ----------------------------------------------------------------------

		String localUserName1 = "user1";
		// EXEC TEST FUNCTION ---------------------------------------------------------------------
		// 创建桶配置
		NamespaceSettings namespaceSetting1 = SettingBuilders.createNamespaceBuilder()
				.withName(ns)
				.withHardQuota(11.2, QuotaUnit.GB)
				.withSoftQuota(66)
				.withHashScheme(HashScheme.SHA512)
				.withMultipartUploadAutoAbortDays(6)
				.withOptimizedFor(OptimizedFor.CLOUD)
				.withAclsUsage(AclsUsage.ENFORCED)
				.withCustomMetadataIndexingEnabled(true)
				.withSearchEnabled(true)
				.withVersioningEnabled(true)
				.withVersioningKeepDeletionRecords(false)
				.withVersioningPrune(false)
				.withVersioningPruneDays(9)
				.withIndexingEnabled(true)
				// .withOwner(OwnerType.LOCAL, localUserName1 )
				// .withEnterpriseMode(true)
				// .withTags("AAA","BBB","中文")
				.bulid();
		// 执行创建桶
		namespaceClient.createNamespace(namespaceSetting1);

		// 验证是否创建
		exist = namespaceClient.doesNamespaceExist(ns);
		System.out.println("Namespece [" + ns + "] " + (exist ? "created!" : "create failed!"));

		System.out.println("Print all namespaces in this tenant:");
		System.out.println("------------------------------------");
		String[] namespaces = namespaceClient.listNamespaces();
		for (String namespace : namespaces) {
			NamespaceSettings setting = namespaceClient.getNamespaceSettings(namespace);
			System.out.println(namespace + " (" + setting.getHardQuota() + " " + setting.getHardQuotaUnit() + ")");
		}
		System.out.println("------------------------------------");

		System.out.println("Well done!");
	}
}
