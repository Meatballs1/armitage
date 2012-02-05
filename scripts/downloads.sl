#
# Loot browser (not yet complete... on hold until more post/ modules have loot)
#

import table.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import ui.*;

sub updateDownloadModel {
	thread(lambda({
		local('$root $files $entry $findf $hosts $host');

		if ($client !is $mclient) {
			$files = call($mclient, "armitage.downloads");
		}
		else {
			$files = listDownloads(downloadDirectory());
		}

		[$model clear: 256];

		foreach $entry ($files) {
			$entry["date"] = rtime($entry["updated_at"] / 1000.0);
			[$model addEntry: $entry];
		}
		[$model fireListeners];
	}, \$model));
}

sub createDownloadBrowser {
	local('$table $model $panel $refresh $sorter $host $view');

	$model = [new GenericTableModel: @("host", "name", "path", "size", "date"), "location", 16];

	$panel = [new JPanel];
	[$panel setLayout: [new BorderLayout]];

	$table = [new ATable: $model];
	setupSizeRenderer($table, "size");
	$sorter = [new TableRowSorter: $model];
        [$sorter toggleSortOrder: 0];
	[$sorter setComparator: 0, &compareHosts];
	[$sorter setComparator: 4, {
		return convertDate($1) <=> convertDate($2);
	}];
	[$table setRowSorter: $sorter];

	[$panel add: [new JScrollPane: $table], [BorderLayout CENTER]];

	addMouseListener($table, lambda({
		if ($0 eq "mousePressed" && [$1 getClickCount] >= 2) {
			showLoot(\$model, \$table);
		}
	}, \$model, \$table));

	$view = [new JButton: "View"];

	[$view addActionListener: lambda({
		showLoot(\$model, \$table);
	}, \$model, \$table)];

	$refresh = [new JButton: "Refresh"];
	[$refresh addActionListener: lambda({
		updateDownloadModel(\$model);	
	}, \$model)];

	updateDownloadModel(\$model); 		

	[$panel add: center($view, $refresh), [BorderLayout SOUTH]];

	[$frame addTab: "Downloads", $panel, $null];
}
